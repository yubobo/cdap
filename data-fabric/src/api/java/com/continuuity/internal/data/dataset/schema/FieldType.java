package com.continuuity.internal.data.dataset.schema;

/**
 *
 */
public final class FieldType {

  private enum Type {
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    STRING,
    BINARY,
    RECORD,
    LIST,
    MAP
  }

  private final Type type;
  private final FieldType subType1, subType2;
  private final Schema recordSchema;

  FieldType(Type type) {
    this(type, null, null, null);
  }

  FieldType(FieldType elementType) {
    this(Type.LIST, elementType, null, null);
  }

  FieldType(FieldType keyType, FieldType valueType) {
    this(Type.MAP, keyType, valueType, null);
  }

  FieldType(Schema recordSchema) {
    this(Type.RECORD, null, null, recordSchema);
  }

  private FieldType(Type type, FieldType subType1, FieldType subType2, Schema recordSchema) {
    this.type = type;
    this.subType1 = subType1;
    this.subType2 = subType2;
    this.recordSchema = recordSchema;
  }

  public static final FieldType INT = new FieldType(Type.INT);
  public static final FieldType LONG = new FieldType(Type.LONG);
  public static final FieldType FLOAT = new FieldType(Type.FLOAT);
  public static final FieldType DOUBLE = new FieldType(Type.DOUBLE);
  public static final FieldType BOOLEAN = new FieldType(Type.BOOLEAN);
  public static final FieldType STRING = new FieldType(Type.STRING);
  public static final FieldType BINARY = new FieldType(Type.BINARY);

  public static FieldType record(Schema schema) {
    return new FieldType(schema);
  }

  public static FieldType map(FieldType keyType, FieldType valueType) {
    return new FieldType(keyType, valueType);
  }

  public static FieldType list(FieldType elementType) {
    return new FieldType(elementType);
  }

  public boolean isRecord() {
    return type == Type.RECORD;
  }

  public boolean isMap() {
    return type == Type.MAP;
  }

  public boolean isList() {
    return type == Type.LIST;
  }

  public FieldType getElementType() {
    if (!isList()) {
      throw new IllegalArgumentException("not a list");
    }
    return subType1;
  }

  public FieldType getKeyType() {
    if (!isMap()) {
      throw new IllegalArgumentException("not a map");
    }
    return subType1;
  }

  public FieldType getValueType() {
    if (!isMap()) {
      throw new IllegalArgumentException("not a map");
    }
    return subType2;
  }

  public Schema getRecordSchema() {
    if (!isRecord()) {
      throw new IllegalArgumentException("not a record");
    }
    return recordSchema;
  }
}
