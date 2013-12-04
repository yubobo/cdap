package com.continuuity.api.data;

import com.continuuity.api.data.schema.Column;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface DataPartition {
  /** Returns the partition key values for this partition. */
  public Map<Column, Object> getPartitionValues();

  /** Returns the defined column schema for this partition for this table. */
  public List<Column> getColumnSchema();
}
