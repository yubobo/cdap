package com.continuuity.hive;

import com.continuuity.api.data.schema.Column;

import java.util.List;

/**
 * Mapping of dataset schemas to HCatalog schema types.
 */
public class HCatSchemaMapping {
  private final List<Column> columns;
  public HCatSchemaMapping(List<Column> columns) {
    this.columns = columns;
  }
}
