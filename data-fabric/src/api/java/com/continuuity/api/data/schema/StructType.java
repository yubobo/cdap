package com.continuuity.api.data.schema;

import java.util.List;

/**
 *
 */
public class StructType extends DataType {
  private List<Column> fields;

  public StructType(List<Column> fields) {
    super(Type.STRUCT);
    this.fields = fields;
  }

  public List<Column> getFields() {
    return fields;
  }
}
