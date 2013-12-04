package com.continuuity.api.data.schema;

/**
 *
 */
public abstract class DataType {
  /**
   * Possible types map to Hive types defined in org.apache.hive.hcatalog.data.schema.HCatFieldSchema.Type
   */
  public enum Type {
    SHORT,
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    STRING,
    ARRAY,
    MAP,
    STRUCT,
    BYTES
  }

  private Type type;
  protected DataType(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}
