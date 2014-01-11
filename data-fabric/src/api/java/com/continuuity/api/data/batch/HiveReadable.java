package com.continuuity.api.data.batch;

/**
 * @param <VALUE> Value type returned by the split reader.
 */
public interface HiveReadable<VALUE> extends BatchReadable<byte[], VALUE> {
  //public DataSetSchema getSchema();

  public Class<VALUE> getValueType();
}
