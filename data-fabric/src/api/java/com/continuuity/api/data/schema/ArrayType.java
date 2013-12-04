package com.continuuity.api.data.schema;

/**
 *
 */
public class ArrayType extends DataType {
  private DataType elementType;

  public ArrayType(DataType elementType) {
    super(Type.ARRAY);
    this.elementType = elementType;
  }

  public DataType getElementType() {
    return elementType;
  }
}
