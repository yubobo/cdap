package com.continuuity.hive;

import com.continuuity.api.data.DataSet;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.discovery.EndpointStrategy;
import com.continuuity.common.discovery.RandomEndpointStrategy;
import com.continuuity.common.discovery.TimeLimitEndpointStrategy;
import com.continuuity.data.DistributedDataSetAccessor;
import com.continuuity.data.operation.OperationContext;
import com.continuuity.data2.util.hbase.HBaseTableUtilFactory;
import com.continuuity.gateway.handlers.dataset.DataSetInstantiatorFromMetaData;
import com.continuuity.weave.discovery.ZKDiscoveryService;
import com.continuuity.weave.filesystem.HDFSLocationFactory;
import com.continuuity.weave.filesystem.LocationFactory;
import com.continuuity.weave.zookeeper.RetryStrategies;
import com.continuuity.weave.zookeeper.ZKClientService;
import com.continuuity.weave.zookeeper.ZKClientServices;
import com.continuuity.weave.zookeeper.ZKClients;
import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DataSetUtil {
  public static DataSet getDataSetInstance(Configuration conf, String accountName, String datasetName)
    throws IOException {
    // application name is not needed for the context
    OperationContext opContext = new OperationContext(accountName, null);
    LocationFactory locationFactory = new HDFSLocationFactory(conf);

    DataSetInstantiatorFromMetaData instantiator =
      new DataSetInstantiatorFromMetaData(locationFactory,
                                          new DistributedDataSetAccessor(CConfiguration.create(), conf, locationFactory,
                                                                         new HBaseTableUtilFactory().get()));
    try {
      instantiator.init(initEndpointStrategy());
    } catch (TException te) {
      throw new IOException(te);
    }
    return instantiator.getDataSet(datasetName, opContext);
  }

  /**
   * Initialize the service discovery client, we will reuse that
   * every time we need to create a new client.
   * @throws org.apache.thrift.TException
   */
  public static EndpointStrategy initEndpointStrategy() throws TException {
    CConfiguration configuration = CConfiguration.create();
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
        new ZKDiscoveryService(zkClientService).discover(com.continuuity.common.conf.Constants.Service.TRANSACTION)),
      2, TimeUnit.SECONDS);
    return endpointStrategy;
  }
}
