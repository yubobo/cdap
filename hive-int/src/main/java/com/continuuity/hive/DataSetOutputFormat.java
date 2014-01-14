package com.continuuity.hive;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.util.Progressable;

import java.io.IOException;

/**
 *
 */
public class DataSetOutputFormat implements OutputFormat<ImmutableBytesWritable, KeyedObjectWritable> {
  @Override
  public RecordWriter<ImmutableBytesWritable, KeyedObjectWritable> getRecordWriter(
    FileSystem ignored, JobConf job, String name, Progressable progress) throws IOException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void checkOutputSpecs(FileSystem ignored, JobConf job) throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
