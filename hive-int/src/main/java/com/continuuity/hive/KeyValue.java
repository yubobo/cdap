package com.continuuity.hive;

import org.apache.hadoop.hbase.io.HbaseObjectWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class KeyValue implements Writable {
  private Object key;
  private Object value;

  public KeyValue(Object key, Object value) {
    this.key = key;
    this.value = value;
  }

  public Object getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    HbaseObjectWritable.writeObject(out, key, key.getClass(), null);
    HbaseObjectWritable.writeObject(out, value, value.getClass(), null);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    key = HbaseObjectWritable.readObject(in, null);
    value = HbaseObjectWritable.readObject(in, null);
  }
}
