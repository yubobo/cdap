package com.continuuity.api.data.schema;

/**
 *
 */
public class MapType extends DataType {
  private DataType keyType;
  private DataType valueType;

  public MapType(DataType keyType, DataType valueType) {
    super(Type.MAP);
    this.keyType = keyType;
    this.valueType = valueType;
  }

  public DataType getKeyType() {
    return keyType;
  }

  public DataType getValueType() {
    return valueType;
  }
}
