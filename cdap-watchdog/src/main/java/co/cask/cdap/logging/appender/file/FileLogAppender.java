/*
 * Copyright © 2014-2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.logging.appender.file;

import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.namespace.NamespacedLocationFactory;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.logging.LoggingConfiguration;
import co.cask.cdap.logging.appender.LogAppender;
import co.cask.cdap.logging.appender.LogMessage;
import co.cask.cdap.logging.save.LogSaverTableUtil;
import co.cask.cdap.logging.serialize.LogSchema;
import co.cask.cdap.logging.serialize.LoggingEvent;
import co.cask.cdap.logging.write.AvroFileWriter;
import co.cask.cdap.logging.write.FileMetaDataManager;
import co.cask.cdap.logging.write.LogCleanup;
import co.cask.cdap.logging.write.LogFileWriter;
import co.cask.cdap.logging.write.LogWriteEvent;
import co.cask.cdap.logging.write.SimpleLogFileWriter;
import co.cask.tephra.TransactionExecutorFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.twill.common.Threads;
import org.apache.twill.filesystem.LocationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Logback appender that writes log events to files.
 */
public class FileLogAppender extends LogAppender {
  private static final Logger LOG = LoggerFactory.getLogger(FileLogAppender.class);

  public static final String APPENDER_NAME = "FileLogAppender";

  private final CConfiguration cConf;
  private final LogSaverTableUtil tableUtil;
  private final TransactionExecutorFactory txExecutorFactory;
  private final LocationFactory locationFactory;
  private final NamespacedLocationFactory namespacedLocationFactory;
  private final String logBaseDir;
  private final int syncIntervalBytes;
  private final long retentionDurationMs;
  private final long maxLogFileSizeBytes;
  private final long inactiveIntervalMs;
  private final long checkpointIntervalMs;
  private final int logCleanupIntervalMins;
  private final ListeningScheduledExecutorService scheduledExecutor;
  private final DatasetFramework dsFramework;

  private final AtomicBoolean stopped = new AtomicBoolean(false);

  private LogFileWriter<LogWriteEvent> logFileWriter;
  private Schema logSchema;

  @Inject
  public FileLogAppender(CConfiguration cConfig,
                         DatasetFramework dsFramework,
                         TransactionExecutorFactory txExecutorFactory,
                         LocationFactory locationFactory,
                         NamespacedLocationFactory namespacedLocationFactory) {
    setName(APPENDER_NAME);
    this.cConf = cConfig;
    this.tableUtil = new LogSaverTableUtil(dsFramework, cConfig);
    this.txExecutorFactory = txExecutorFactory;
    this.locationFactory = locationFactory;
    this.namespacedLocationFactory = namespacedLocationFactory;
    this.dsFramework = dsFramework;

    this.logBaseDir = cConfig.get(LoggingConfiguration.LOG_BASE_DIR);
    Preconditions.checkNotNull(logBaseDir, "Log base dir cannot be null");

    this.syncIntervalBytes = cConfig.getInt(LoggingConfiguration.LOG_FILE_SYNC_INTERVAL_BYTES, 50 * 1024);
    Preconditions.checkArgument(this.syncIntervalBytes > 0,
                                "Log file sync interval is invalid: %s", this.syncIntervalBytes);

    long retentionDurationDays = cConfig.getLong(LoggingConfiguration.LOG_RETENTION_DURATION_DAYS, -1);
    Preconditions.checkArgument(retentionDurationDays > 0,
                                "Log file retention duration is invalid: %s", retentionDurationDays);
    this.retentionDurationMs = TimeUnit.MILLISECONDS.convert(retentionDurationDays, TimeUnit.DAYS);

    maxLogFileSizeBytes = cConfig.getLong(LoggingConfiguration.LOG_MAX_FILE_SIZE_BYTES, 20 * 1024 * 1024);
    Preconditions.checkArgument(maxLogFileSizeBytes > 0,
                                "Max log file size is invalid: %s", maxLogFileSizeBytes);

    inactiveIntervalMs = cConfig.getLong(LoggingConfiguration.LOG_SAVER_INACTIVE_FILE_INTERVAL_MS,
                                              LoggingConfiguration.DEFAULT_LOG_SAVER_INACTIVE_FILE_INTERVAL_MS);
    Preconditions.checkArgument(inactiveIntervalMs > 0,
                                "Inactive interval is invalid: %s", inactiveIntervalMs);

    checkpointIntervalMs = cConfig.getLong(LoggingConfiguration.LOG_SAVER_CHECKPOINT_INTERVAL_MS,
                                                LoggingConfiguration.DEFAULT_LOG_SAVER_CHECKPOINT_INTERVAL_MS);
    Preconditions.checkArgument(checkpointIntervalMs > 0,
                                "Checkpoint interval is invalid: %s", checkpointIntervalMs);

    logCleanupIntervalMins = cConfig.getInt(LoggingConfiguration.LOG_CLEANUP_RUN_INTERVAL_MINS,
                                            LoggingConfiguration.DEFAULT_LOG_CLEANUP_RUN_INTERVAL_MINS);
    Preconditions.checkArgument(logCleanupIntervalMins > 0,
                                "Log cleanup run interval is invalid: %s", logCleanupIntervalMins);

    this.scheduledExecutor =
      MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor(
        Threads.createDaemonThreadFactory("file-log-appender")));
  }

  @Override
  public void start() {
    super.start();
    try {
      logSchema = new LogSchema().getAvroSchema();
      FileMetaDataManager fileMetaDataManager = new FileMetaDataManager(tableUtil, txExecutorFactory,
                                                                        locationFactory, cConf);

      AvroFileWriter avroFileWriter = new AvroFileWriter(fileMetaDataManager, cConf, locationFactory.create(""),
                                                         logBaseDir, logSchema, maxLogFileSizeBytes, syncIntervalBytes,
                                                         inactiveIntervalMs);
      logFileWriter = new SimpleLogFileWriter(avroFileWriter, checkpointIntervalMs);

      String namespacesDir = cConf.get(Constants.Namespace.NAMESPACES_DIR);
      LogCleanup logCleanup = new LogCleanup(fileMetaDataManager, locationFactory.create(""), namespacesDir,
                                             retentionDurationMs);
      scheduledExecutor.scheduleAtFixedRate(logCleanup, 10,
                                            logCleanupIntervalMins, TimeUnit.MINUTES);
    } catch (Exception e) {
      close();
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected void append(LogMessage logMessage) {
    try {
      GenericRecord datum = LoggingEvent.encode(logSchema, logMessage.getLoggingEvent(),
                                                logMessage.getLoggingContext());
      logFileWriter.append(ImmutableList.of(new LogWriteEvent(datum, logMessage.getLoggingEvent(),
                                                              logMessage.getLoggingContext())));
    } catch (Throwable t) {
      LOG.error("Got exception while serializing log event {}.", logMessage.getLoggingEvent(), t);
    }
  }

  private void close() {
    try {
      if (logFileWriter != null) {
        logFileWriter.close();
      }
    } catch (IOException e) {
      LOG.error("Got exception while closing logFileWriter", e);
    }
  }

  @Override
  public void stop() {
    if (!stopped.compareAndSet(false, true)) {
      return;
    }

    scheduledExecutor.shutdownNow();
    close();
    super.stop();
  }
}
