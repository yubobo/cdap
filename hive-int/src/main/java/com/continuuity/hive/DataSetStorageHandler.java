package com.continuuity.hive;

import org.apache.hadoop.hive.ql.metadata.DefaultStorageHandler;
import org.apache.hadoop.hive.ql.plan.TableDesc;
import org.apache.hadoop.hive.serde2.SerDe;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;

import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class DataSetStorageHandler extends DefaultStorageHandler {
  public static final String TABLE_NAME_CONF_KEY = "continuuity.table.name";

  @Override
  public Class<? extends InputFormat> getInputFormatClass() {
    return DataSetInputFormat.class;
  }

  @Override
  public Class<? extends OutputFormat> getOutputFormatClass() {
    return DataSetOutputFormat.class;  // not yet supported
  }

  @Override
  public Class<? extends SerDe> getSerDeClass() {
    return DataSetSerDe.class;
  }

  @Override
  public void configureInputJobProperties(TableDesc tableDesc, Map<String, String> jobProperties) {
    configureTableJobProperties(tableDesc, jobProperties);
  }

  @Override
  public void configureOutputJobProperties(TableDesc tableDesc, Map<String, String> jobProperties) {
    configureTableJobProperties(tableDesc, jobProperties);
  }

  @Override
  public void configureTableJobProperties(TableDesc tableDesc, Map<String, String> jobProperties) {
    Properties tableProps = tableDesc.getProperties();
    jobProperties.put(DataSetSerDe.DATASET_ACCOUNT_KEY, tableProps.getProperty(DataSetSerDe.DATASET_ACCOUNT_KEY));
    jobProperties.put(DataSetSerDe.DATASET_NAME_KEY, tableProps.getProperty(DataSetSerDe.DATASET_NAME_KEY));
    jobProperties.put(TABLE_NAME_CONF_KEY, tableDesc.getTableName());
  }

  @Override
  public void configureJobConf(TableDesc tableDesc, JobConf jobConf) {
    // TODO: add dependency jars
    Properties tableProps = tableDesc.getProperties();
    jobConf.set(DataSetSerDe.DATASET_ACCOUNT_KEY, tableProps.getProperty(DataSetSerDe.DATASET_ACCOUNT_KEY));
    jobConf.set(DataSetSerDe.DATASET_NAME_KEY, tableProps.getProperty(DataSetSerDe.DATASET_NAME_KEY));
  }
}
