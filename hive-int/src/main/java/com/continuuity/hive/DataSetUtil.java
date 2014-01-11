package com.continuuity.hive;

import com.continuuity.api.data.DataSet;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data.DistributedDataSetAccessor;
import com.continuuity.data.operation.OperationContext;
import com.continuuity.data2.util.hbase.HBaseTableUtilFactory;
import com.continuuity.gateway.handlers.dataset.DataSetInstantiatorFromMetaData;
import com.continuuity.weave.filesystem.HDFSLocationFactory;
import com.continuuity.weave.filesystem.LocationFactory;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

/**
 *
 */
public class DataSetUtil {
  public static DataSet getDataSetInstance(Configuration conf, String accountName, String datasetName)
    throws IOException {
    // application name is not needed for the context
    OperationContext opContext = new OperationContext(accountName, "");
    LocationFactory locationFactory = new HDFSLocationFactory(conf);

    DataSetInstantiatorFromMetaData instantiator =
      new DataSetInstantiatorFromMetaData(locationFactory,
                                          new DistributedDataSetAccessor(CConfiguration.create(), conf, locationFactory,
                                                                         new HBaseTableUtilFactory().get()));
    return instantiator.getDataSet(datasetName, opContext);
  }
}
