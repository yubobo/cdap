package com.continuuity.explore.service.hive;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data2.dataset2.DatasetFramework;
import com.continuuity.data2.transaction.Transaction;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.explore.service.ColumnDesc;
import com.continuuity.explore.service.ExploreException;
import com.continuuity.explore.service.ExploreService;
import com.continuuity.explore.service.Handle;
import com.continuuity.explore.service.HandleNotFoundException;
import com.continuuity.explore.service.Result;
import com.continuuity.explore.service.Status;
import com.continuuity.hive.context.CConfCodec;
import com.continuuity.hive.context.ConfigurationUtil;
import com.continuuity.hive.context.ContextManager;
import com.continuuity.hive.context.HConfCodec;
import com.continuuity.hive.context.TxnCodec;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractIdleService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hive.service.cli.CLIService;
import org.apache.hive.service.cli.ColumnDescriptor;
import org.apache.hive.service.cli.HiveSQLException;
import org.apache.hive.service.cli.OperationHandle;
import org.apache.hive.service.cli.SessionHandle;
import org.apache.hive.service.cli.TableSchema;
import org.apache.twill.common.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Defines common functionality used by different HiveExploreServices. The common functionality includes
 * starting/stopping transactions, serializing configuration and saving operation information.
 */
public abstract class BaseHiveExploreService extends AbstractIdleService implements ExploreService {
  private static final Logger LOG = LoggerFactory.getLogger(BaseHiveExploreService.class);

  private final CConfiguration cConf;
  private final Configuration hConf;
  private final HiveConf hiveConf;
  private final TransactionSystemClient txClient;

  // Handles that are running, or not yet completely fetched, they have longer timeout
  private final Cache<Handle, OperationInfo> activeHandleCache;
  // Handles that don't have any more results to be fetched, they can be timed out aggressively.
  private final Cache<Handle, InactiveOperationInfo> inactiveHandleCache;

  private final ExecutorService executingQueriesPool;

  private final CLIService cliService;
  private final ScheduledExecutorService scheduledExecutorService;
  private final long cleanupJobSchedule;

  protected abstract Status fetchStatus(OperationHandle handle) throws HiveSQLException, ExploreException,
    HandleNotFoundException;
  protected abstract List<Result> fetchNextResults(OperationHandle handle, int size)
    throws HiveSQLException, ExploreException, HandleNotFoundException;

  protected BaseHiveExploreService(TransactionSystemClient txClient, DatasetFramework datasetFramework,
                                   CConfiguration cConf, Configuration hConf, HiveConf hiveConf) {
    this.cConf = cConf;
    this.hConf = hConf;
    this.hiveConf = hiveConf;

    this.scheduledExecutorService =
      Executors.newSingleThreadScheduledExecutor(Threads.createDaemonThreadFactory("explore-handle-timeout"));

    this.activeHandleCache =
      CacheBuilder.newBuilder()
        .expireAfterWrite(cConf.getLong(Constants.Explore.ACTIVE_OPERATION_TIMEOUT_SECS), TimeUnit.SECONDS)
        .removalListener(new ActiveOperationRemovalHandler(this, scheduledExecutorService))
        .build();
    this.inactiveHandleCache =
      CacheBuilder.newBuilder()
        .expireAfterWrite(cConf.getLong(Constants.Explore.INACTIVE_OPERATION_TIMEOUT_SECS), TimeUnit.SECONDS)
        .build();

    this.cliService = new CLIService();

    this.txClient = txClient;
    ContextManager.saveContext(datasetFramework);

    cleanupJobSchedule = cConf.getLong(Constants.Explore.CLEANUP_JOB_SCHEDULE_SECS);

    // TODO create a constant for that 100
    executingQueriesPool = Executors.newFixedThreadPool(100);

    LOG.info("Active handle timeout = {} secs", cConf.getLong(Constants.Explore.ACTIVE_OPERATION_TIMEOUT_SECS));
    LOG.info("Inactive handle timeout = {} secs", cConf.getLong(Constants.Explore.INACTIVE_OPERATION_TIMEOUT_SECS));
    LOG.info("Cleanup job schedule = {} secs", cleanupJobSchedule);
  }

  protected HiveConf getHiveConf() {
    // TODO figure out why this hive conf does not contain our env properties - REACTOR-270
    // return hiveConf;
    return new HiveConf();
  }

  protected CLIService getCliService() {
    return cliService;
  }

  @Override
  protected void startUp() throws Exception {
    LOG.info("Starting {}...", Hive13ExploreService.class.getSimpleName());
    cliService.init(getHiveConf());
    cliService.start();
    // TODO: Figure out a way to determine when cliService has started successfully - REACTOR-254
    TimeUnit.SECONDS.sleep(5);

    // Schedule the cache cleanup
    scheduledExecutorService.scheduleWithFixedDelay(
      new Runnable() {
        @Override
        public void run() {
          runCacheCleanup();
        }
      }, cleanupJobSchedule, cleanupJobSchedule, TimeUnit.SECONDS
    );
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.info("Stopping {}...", BaseHiveExploreService.class.getSimpleName());

    // By this time we should not get anymore new requests, since HTTP service has already been stopped.
    // Close all handles
    if (!activeHandleCache.asMap().isEmpty()) {
      LOG.info("Timing out active handles...");
    }
    activeHandleCache.invalidateAll();
    // Make sure the cache entries get expired.
    runCacheCleanup();

    // Wait for all cleanup jobs to complete
    scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS);
    scheduledExecutorService.shutdown();

    cliService.stop();
  }

  @Override
  public Handle execute(final String statement) throws ExploreException {
    Map<String, String> sessionConf = null;
    try {
      sessionConf = startSession();
      // TODO: allow changing of hive user and password - REACTOR-271
      final SessionHandle sessionHandle = cliService.openSession("hive", "", sessionConf);
      Future<OperationHandle> futureHandle = executingQueriesPool.submit(new Callable<OperationHandle>() {
        @Override
        public OperationHandle call() throws Exception {
          OperationHandle operationHandle =
            cliService.executeStatement(sessionHandle, statement, ImmutableMap.<String, String>of());
          return operationHandle;
        }
      });
      Handle handle = saveOperationInfo(futureHandle, sessionHandle, sessionConf);
      LOG.trace("Executing statement: {} with handle {}", statement, handle);
      return handle;
    } catch (HiveSQLException e) {
      throw getSqlException(e);
    } catch (IOException e) {
      throw new ExploreException(e);
    }
  }

  @Override
  public Status getStatus(Handle handle) throws ExploreException, HandleNotFoundException {
    InactiveOperationInfo inactiveOperationInfo = inactiveHandleCache.getIfPresent(handle);
    if (inactiveOperationInfo != null) {
      // Operation has been made inactive, so return the saved status.
      LOG.trace("Returning saved status for inactive handle {}", handle);
      return inactiveOperationInfo.getStatus();
    }

    try {
      OperationHandle operationHandle = getOperationHandle(handle);
      if (operationHandle == null) {
        return new Status(Status.OpStatus.RUNNING, false);
      }

      // Fetch status from Hive
      Status status = fetchStatus(operationHandle);
      LOG.trace("Status of handle {} is {}", handle, status);

      if ((status.getStatus() == Status.OpStatus.FINISHED && !status.hasResults()) ||
        status.getStatus() == Status.OpStatus.ERROR) {
        // No results or error, so can be timed out aggressively
        timeoutAggresively(handle, getResultSchema(handle), status);
      }
      return status;
    } catch (HiveSQLException e) {
      throw getSqlException(e);
    }
  }

  @Override
  public List<Result> nextResults(Handle handle, int size) throws ExploreException, HandleNotFoundException {
    InactiveOperationInfo inactiveOperationInfo = inactiveHandleCache.getIfPresent(handle);
    if (inactiveOperationInfo != null) {
      // Operation has been made inactive, so all results should have been fetched already - return empty list.
      LOG.trace("Returning empty result for inactive handle {}", handle);
      return ImmutableList.of();
    }

    try {
      // Fetch results from Hive
      LOG.trace("Getting results for handle {}", handle);
      OperationHandle operationHandle = getOperationHandle(handle);
      if (operationHandle == null) {
        return Lists.newArrayList();
      }

      List<Result> results = fetchNextResults(operationHandle, size);

      Status status = getStatus(handle);
      if (results.isEmpty() && status.getStatus() == Status.OpStatus.FINISHED) {
        // Since operation has fetched all the results, handle can be timed out aggressively.
        timeoutAggresively(handle, getResultSchema(handle), status);
      }
      return results;
    } catch (HiveSQLException e) {
      throw getSqlException(e);
    }
  }

  @Override
  public List<ColumnDesc> getResultSchema(Handle handle) throws ExploreException, HandleNotFoundException {
    try {
      InactiveOperationInfo inactiveOperationInfo = inactiveHandleCache.getIfPresent(handle);
      if (inactiveOperationInfo != null) {
        // Operation has been made inactive, so return saved schema.
        LOG.trace("Returning saved schema for inactive handle {}", handle);
        return inactiveOperationInfo.getSchema();
      }

      // Fetch schema from hive
      LOG.trace("Getting schema for handle {}", handle);
      ImmutableList.Builder<ColumnDesc> listBuilder = ImmutableList.builder();
      OperationHandle operationHandle = getOperationHandle(handle);
      if (operationHandle == null) {
        // We can't access the schema of the results before execution is complete
        return Lists.newArrayList();
      }

      if (operationHandle.hasResultSet()) {
        TableSchema tableSchema = cliService.getResultSetMetadata(operationHandle);
        for (ColumnDescriptor colDesc : tableSchema.getColumnDescriptors()) {
          listBuilder.add(new ColumnDesc(colDesc.getName(), colDesc.getTypeName(),
                                         colDesc.getOrdinalPosition(), colDesc.getComment()));
        }
      }
      return listBuilder.build();
    } catch (HiveSQLException e) {
      throw getSqlException(e);
    }
  }

  @Override
  public void cancel(Handle handle) throws ExploreException, HandleNotFoundException {
    try {
      InactiveOperationInfo inactiveOperationInfo = inactiveHandleCache.getIfPresent(handle);
      if (inactiveOperationInfo != null) {
        // Operation has been made inactive, so no point in cancelling it.
        LOG.trace("Not running cancel for inactive handle {}", handle);
        return;
      }

      LOG.trace("Cancelling operation {}", handle);
      OperationHandle operationHandle = getOperationHandle(handle);
      // TODO think about what it means to cancel without a operationHandle
      if (operationHandle == null) {
        // Cancel the executor by interrupting the thread / preventing it to start
        getFutureOperationHandle(handle).cancel(true);
      } else {
        cliService.cancelOperation(operationHandle);
      }

      // Since operation is cancelled, we can aggressively time it out.
      timeoutAggresively(handle, ImmutableList.<ColumnDesc>of(), new Status(Status.OpStatus.CANCELED, false));
    } catch (HiveSQLException e) {
      throw getSqlException(e);
    }
  }

  @Override
  public void close(Handle handle) throws ExploreException, HandleNotFoundException {
    activeHandleCache.invalidate(handle);
  }

  void closeInternal(Handle handle, OperationInfo opInfo) throws ExploreException, HandleNotFoundException {
    try {
      LOG.trace("Closing operation {}", handle);

      OperationHandle operationHandle = getOperationHandle(handle);
      // TODO think about what it means to close without a operationHandle
      if (operationHandle == null) {
        return;
      }

      cliService.closeOperation(operationHandle);
    } catch (HiveSQLException e) {
      throw getSqlException(e);
    } finally {
      try {
        closeSession(opInfo.getSessionHandle());
      } finally {
        cleanUp(handle, opInfo);
      }
    }
  }

  private void closeSession(SessionHandle sessionHandle) {
    try {
      cliService.closeSession(sessionHandle);
    } catch (Throwable e) {
      LOG.error("Got error closing session", e);
    }
  }

  /**
   * Starts a long running transaction, and also sets up session configuration.
   * @return configuration for a hive session that contains a transaction, and serialized reactor configuration and
   * HBase configuration. This will be used by the map-reduce tasks started by Hive.
   * @throws IOException
   */
  protected Map<String, String> startSession() throws IOException {
    Map<String, String> sessionConf = Maps.newHashMap();

    Transaction tx = startTransaction();
    ConfigurationUtil.set(sessionConf, Constants.Explore.TX_QUERY_KEY, TxnCodec.INSTANCE, tx);
    ConfigurationUtil.set(sessionConf, Constants.Explore.CCONF_KEY, CConfCodec.INSTANCE, cConf);
    ConfigurationUtil.set(sessionConf, Constants.Explore.HCONF_KEY, HConfCodec.INSTANCE, hConf);
    return sessionConf;
  }

  /**
   * Returns {@link Future<OperationHandle>} associated with Explore {@link Handle}.
   * @param handle explore handle.
   * @return OperationHandle.
   * @throws ExploreException
   */
  protected Future<OperationHandle> getFutureOperationHandle(Handle handle)
    throws ExploreException, HandleNotFoundException {
    return getOperationInfo(handle).getFutureOperationHandle();
  }

  protected OperationHandle getOperationHandle(Handle handle)
    throws ExploreException, HandleNotFoundException {
    try {
      return getFutureOperationHandle(handle).get(20, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      // Future object is still in progress
      return null;
    } catch (HandleNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new ExploreException(e);
    }
  }

  /**
   * Saves information associated with an Hive operation.
   * @param futureOperationHandle {@link Future<OperationHandle>} of the Hive operation running.
   * @param sessionHandle {@link SessionHandle} for the Hive operation running.
   * @param sessionConf configuration for the session running the Hive operation.
   * @return {@link Handle} that represents the Hive operation being run.
   */
  protected Handle saveOperationInfo(Future<OperationHandle> futureOperationHandle, SessionHandle sessionHandle,
                                     Map<String, String> sessionConf) {
    Handle handle = Handle.generate();
    activeHandleCache.put(handle, new OperationInfo(sessionHandle, futureOperationHandle, sessionConf));
    return handle;
  }

  /**
   * Called after a handle has been used to fetch all its results. This handle can be timed out aggressively.
   *
   * @param handle operation handle.
   */
  private void timeoutAggresively(Handle handle, List<ColumnDesc> schema, Status status)
    throws HandleNotFoundException {
    OperationInfo opInfo = activeHandleCache.getIfPresent(handle);
    if (opInfo == null) {
      LOG.trace("Could not find OperationInfo for handle {}, it might already have been moved to inactive list",
                handle);
      return;
    }

    LOG.trace("Timing out handle {} aggressively", handle);
    inactiveHandleCache.put(handle, new InactiveOperationInfo(opInfo, schema, status));
    activeHandleCache.invalidate(handle);
  }

  private OperationInfo getOperationInfo(Handle handle) throws HandleNotFoundException {
    // First look in running handles and handles that still can be fetched.
    OperationInfo opInfo = activeHandleCache.getIfPresent(handle);
    if (opInfo != null) {
      return opInfo;
    }
    throw new HandleNotFoundException("Invalid handle provided");
  }

  /**
   * Cleans up the metadata associated with active {@link Handle}. It also closes associated transaction.
   * @param handle handle of the running Hive operation.
   */
  protected void cleanUp(Handle handle, OperationInfo opInfo) {
    try {
      closeTransaction(handle, opInfo);
    } finally {
      activeHandleCache.invalidate(handle);
    }
  }

  private Transaction startTransaction() throws IOException {
    Transaction tx = txClient.startLong();
    LOG.trace("Transaction {} started.", tx);
    return tx;
  }

  private void closeTransaction(Handle handle, OperationInfo opInfo) {
    try {
      Transaction tx = ConfigurationUtil.get(opInfo.getSessionConf(),
                                             Constants.Explore.TX_QUERY_KEY,
                                             TxnCodec.INSTANCE);
      LOG.trace("Closing transaction {} for handle {}", tx, handle);

      // Transaction doesn't involve any changes. We still commit it to take care of any side effect changes that
      // SplitReader may have.
      if (!(txClient.canCommit(tx, ImmutableList.<byte[]>of()) && txClient.commit(tx))) {
        txClient.abort(tx);
        LOG.info("Aborting transaction: {}", tx);
      }
    } catch (Throwable e) {
      LOG.error("Got exception while closing transaction.", e);
    }
  }

  private void runCacheCleanup() {
    LOG.trace("Running cache cleanup");
    activeHandleCache.cleanUp();
    inactiveHandleCache.cleanUp();
  }

  private RuntimeException getSqlException(HiveSQLException e) throws ExploreException {
    if (e.getSQLState() != null) {
      throw new IllegalArgumentException(String.format("[SQLState %s] %s", e.getSQLState(), e.getMessage()));
    }
    throw new ExploreException(e);
  }

  /**
  * Helper class to store information about a Hive operation in progress.
  */
  static class OperationInfo {
    private final SessionHandle sessionHandle;
    // private final OperationHandle operationHandle;
    private final Future<OperationHandle> futureOperationHandle;
    private final Map<String, String> sessionConf;

    OperationInfo(SessionHandle sessionHandle, Future<OperationHandle> futureOperationHandle,
                  Map<String, String> sessionConf) {
      this.sessionHandle = sessionHandle;
      this.futureOperationHandle = futureOperationHandle;
      this.sessionConf = sessionConf;
    }

    public SessionHandle getSessionHandle() {
      return sessionHandle;
    }

    public OperationHandle getOperationHandle() throws ExploreException {
      try {
        return futureOperationHandle.get(20, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        // Future object is still in progress
        return null;
      } catch (Exception e) {
        throw new ExploreException(e);
      }
    }

    public Future<OperationHandle> getFutureOperationHandle() {
      return futureOperationHandle;
    }

    public Map<String, String> getSessionConf() {
      return sessionConf;
    }
  }

  private static class InactiveOperationInfo extends OperationInfo {
    private final List<ColumnDesc> schema;
    private final Status status;

    private InactiveOperationInfo(OperationInfo operationInfo, List<ColumnDesc> schema, Status status) {
      super(operationInfo.getSessionHandle(), operationInfo.getFutureOperationHandle(), operationInfo.getSessionConf());
      this.schema = schema;
      this.status = status;
    }

    public List<ColumnDesc> getSchema() {
      return schema;
    }

    public Status getStatus() {
      return status;
    }
  }
}
