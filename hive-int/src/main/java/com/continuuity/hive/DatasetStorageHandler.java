package com.continuuity.hive;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaHook;
import org.apache.hadoop.hive.ql.metadata.DefaultStorageHandler;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.metadata.HiveStorageHandler;
import org.apache.hadoop.hive.ql.plan.TableDesc;
import org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider;
import org.apache.hadoop.hive.serde2.SerDe;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;

import java.util.Map;

/**
 *
 */
public class DataSetStorageHandler extends DefaultStorageHandler {
  @Override
  public Class<? extends InputFormat> getInputFormatClass() {
    return DataSetInputFormat.class;
  }

  @Override
  public Class<? extends OutputFormat> getOutputFormatClass() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Class<? extends SerDe> getSerDeClass() {
    return DataSetSerDe.class;
  }

  @Override
  public void configureInputJobProperties(TableDesc tableDesc, Map<String, String> stringStringMap) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void configureOutputJobProperties(TableDesc tableDesc, Map<String, String> stringStringMap) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void configureTableJobProperties(TableDesc tableDesc, Map<String, String> stringStringMap) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void configureJobConf(TableDesc tableDesc, JobConf entries) {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
