package com.continuuity.hive;

import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.DataSetSpecification;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.discovery.EndpointStrategy;
import com.continuuity.common.discovery.RandomEndpointStrategy;
import com.continuuity.common.discovery.TimeLimitEndpointStrategy;
import com.continuuity.data.DataFabric;
import com.continuuity.data.DataFabric2Impl;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data.DistributedDataSetAccessor;
import com.continuuity.data.dataset.DataSetInstantiator;
import com.continuuity.data.operation.OperationContext;
import com.continuuity.data2.transaction.Transaction;
import com.continuuity.data2.transaction.TransactionAware;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.data2.transaction.distributed.TransactionServiceClient;
import com.continuuity.data2.util.hbase.HBaseTableUtilFactory;
import com.continuuity.gateway.handlers.dataset.DataSetInstantiatorFromMetaData;
import com.continuuity.weave.discovery.ZKDiscoveryService;
import com.continuuity.weave.filesystem.HDFSLocationFactory;
import com.continuuity.weave.filesystem.LocationFactory;
import com.continuuity.weave.zookeeper.RetryStrategies;
import com.continuuity.weave.zookeeper.ZKClientService;
import com.continuuity.weave.zookeeper.ZKClientServices;
import com.continuuity.weave.zookeeper.ZKClients;
import com.google.common.base.Throwables;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DataSetUtil {
  private CConfiguration configuration = CConfiguration.create();

  private Configuration conf;
  private LocationFactory locationFactory;
  private TransactionSystemClient txClient;

  public DataSetUtil(Configuration conf) {
    this.conf = conf;
    this.locationFactory = new HDFSLocationFactory(conf);
    try {
      this.txClient = new TransactionServiceClient(configuration);
    } catch (TException te) {
      throw Throwables.propagate(te);
    }
  }

  public static String getDummyPath(Configuration conf, String tableName) {
    //return locationFactory.getHomeLocation().getName();
    // TODO: for testing
    String tablePath = tableName;
    int lastPeriod = tablePath.lastIndexOf(".");
    if (lastPeriod >= 0 && lastPeriod < tablePath.length() - 1) {
      tablePath = tablePath.substring(lastPeriod + 1);
    }
    String hiveDir = HiveConf.getVar(conf, HiveConf.ConfVars.METASTOREWAREHOUSE);
    Path p = new Path(hiveDir, tablePath);
    return p.toString();
  }

  public DataSetSpecification getDataSetSpecification(Configuration conf, String accountName,
                                                             String datasetName) throws IOException {
    // application name is not needed for the context
    OperationContext opContext = new OperationContext(accountName, null);
    DataSetAccessor accessor = new DistributedDataSetAccessor(CConfiguration.create(), conf, locationFactory,
                                                              new HBaseTableUtilFactory().get());

    DataSetInstantiatorFromMetaData instantiator =
      new DataSetInstantiatorFromMetaData(locationFactory, accessor);

    try {
      instantiator.init(initEndpointStrategy());
    } catch (TException te) {
      throw new IOException(te);
    }
    return instantiator.getDataSetSpecification(datasetName, opContext);
  }

  public DataSet getDataSetInstance(Configuration conf, DataSetSpecification spec) throws IOException {
    LocationFactory locationFactory = new HDFSLocationFactory(conf);
    DataSetAccessor accessor = new DistributedDataSetAccessor(CConfiguration.create(), conf, locationFactory,
                                                              new HBaseTableUtilFactory().get());
    DataFabric dataFabric = new DataFabric2Impl(locationFactory, accessor);

    DataSetInstantiator instantiator = new DataSetInstantiator(dataFabric, DataSetUtil.class.getClassLoader());
    instantiator.addDataSet(spec);
    DataSet ds = instantiator.getDataSet(spec.getName());
    Transaction tx = txClient.startLong();
    for (TransactionAware txAware : instantiator.getTransactionAware()) {
      txAware.startTx(tx);
    }
    return ds;
  }

  /**
   * Initialize the service discovery client, we will reuse that
   * every time we need to create a new client.
   * @throws org.apache.thrift.TException
   */
  public EndpointStrategy initEndpointStrategy() throws TException {
    // try to find the zookeeper ensemble in the config
    String zookeeper = configuration.get(Constants.Zookeeper.QUORUM);
    if (zookeeper == null) {
      // no zookeeper, look for the port and use localhost
      //Log.info("Zookeeper Ensemble not configured. Skipping service discovery");
      throw new IllegalArgumentException("Zookeeper quorum must be configured with property: " +
                                            Constants.Zookeeper.QUORUM);
    }
    // attempt to discover the service
    // Ideally the DiscoveryServiceClient should be injected so that we don't need to create a new ZK client
    // Also, there should be a stop() method for lifecycle management to stop the ZK client
    // Although it's ok for now as ZKClientService uses daemon thread only
    ZKClientService zkClientService =
      ZKClientServices.delegate(ZKClients.reWatchOnExpire(ZKClients.retryOnFailure(
        ZKClientService.Builder.of(zookeeper).build(),
        RetryStrategies.exponentialDelay(500, 2000, TimeUnit.MILLISECONDS))));    zkClientService.startAndWait();
    EndpointStrategy endpointStrategy = new TimeLimitEndpointStrategy(
      new RandomEndpointStrategy(
        new ZKDiscoveryService(zkClientService).discover(Constants.Service.APP_FABRIC)),
      2, TimeUnit.SECONDS);
    return endpointStrategy;
  }
}
