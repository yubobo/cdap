package com.continuuity.api.data.schema;

import com.continuuity.api.data.DataPartition;

import java.util.List;

/**
 *
 */
public interface SchemaService {
  public List<DataPartition> listPartitions(String table);
}
