package com.continuuity.hive;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.ObjectWritable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Extends the generic ObjectWritable with a byte array key.
 */
public class KeyedObjectWritable extends ObjectWritable {
  private ImmutableBytesWritable keyWritable = new ImmutableBytesWritable();

  public byte[] getKey() {
    return keyWritable.get();
  }

  public void setKey(byte[] bytes) {
    keyWritable.set(bytes);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    keyWritable.readFields(in);
    super.readFields(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    keyWritable.write(out);
    super.write(out);
  }
}
