package com.continuuity.api.data;

import com.continuuity.api.data.schema.Column;

import java.util.List;

/**
 *
 */
public interface DataSetTable {
  public String getName();

  public String getOwner();

  public List<Column> getPartitionKeys();

  // FIXME: belongs in a service not in table itself
  public List<DataPartition> listPartitions();
}
