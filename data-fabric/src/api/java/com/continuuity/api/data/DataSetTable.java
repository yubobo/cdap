package com.continuuity.api.data;

import com.continuuity.api.data.schema.Column;

import java.util.List;

/**
 *
 */
public interface DataSetTable {
  /**
   * Returns the local name for this table.  The full table name will be qualified by namespace in the
   * global context. */
  public String getName();

  /**
   * Returns the user owning the table.
   * TODO: should be provided by the framework instead.
   */
  public String getOwner();

  public List<Column> getDataColumns();
}
