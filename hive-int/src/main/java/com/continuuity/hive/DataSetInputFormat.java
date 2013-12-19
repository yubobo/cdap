package com.continuuity.hive;

import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

/**
 * Map reduce InputFormat for reading from data sets.
 * @param <K> Class type of record keys.
 * @param <V> Class type of record values.
 */
public class DataSetInputFormat<K, V> implements InputFormat<K, V> {
  @Override
  public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
    return new InputSplit[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public RecordReader<K, V> getRecordReader(InputSplit split, JobConf job, Reporter reporter) throws IOException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
