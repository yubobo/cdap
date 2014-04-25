package com.continuuity.internal.data.dataset.schema;

import com.google.common.base.Objects;

import java.util.List;
import java.util.Map;

/**
 * Represents the type of a field in a schema: Either a primitive type, a list, a map, or a record.
 */
public final class FieldType {

  private enum Type {

    INT(true, Integer.class),
    LONG(true, Long.class),
    FLOAT(true, Float.class),
    DOUBLE(true, Double.class),
    BOOLEAN(true, Boolean.class),
    STRING(true, String.class),
    BINARY(true, byte[].class),
    RECORD(false, Record.class),
    LIST(false, List.class),
    MAP(false, Map.class);

    private final boolean scalar;
    private final Class<?> classType;

    private Type(boolean isScalar, Class<?> ctype) {
      scalar = isScalar;
      classType = ctype;
    }

    public boolean isScalar() {
      return scalar;
    }

    public void validateType(Object value) {
      if (value != null && !classType.isInstance(value)) {
        throw new IllegalArgumentException("Invalid assignment of " + value.getClass() + " for field type " + name());
      }
    }
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

  public boolean isScalar() {
    return type.isScalar();
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

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }
    FieldType ft = (FieldType) other;
    return type.equals(ft.type)
      && Objects.equal(recordSchema, ft.recordSchema)
      && Objects.equal(subType1, ft.subType1)
      && Objects.equal(subType2, ft.subType2);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(type, recordSchema, subType1, subType2);
  }

  public void validateType(Object value, Validate validate) {
    if (value == null || validate == Validate.OFF) {
      return;
    }
    type.validateType(value);
    if (type.isScalar() || validate == Validate.SHALLOW) {
      return;
    }
    if (isList()) {
      for (Object element : (List<?>) value) {
        getElementType().validateType(element, validate);
        if (validate == Validate.RELAXED && element != null) {
          break; // if relaxed, stop after first non-null element
        }
      }
    } else if (isMap()) {
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        getKeyType().validateType(entry.getKey(), validate);
        getValueType().validateType(entry.getValue(), validate);
        if (validate == Validate.RELAXED && entry.getValue() != null) {
          break; // if relaxed, stop after first non-null value
        }
      }
    } else if (isRecord()) {
      if (value instanceof DefaultRecord) {
        Schema valueSchema = ((DefaultRecord) value).getSchema();
        if (valueSchema != null) {
          if (!getRecordSchema().equals(valueSchema)) {
            throw new IllegalArgumentException("Assignment of incompatible record type");
          } else {
            return;
          }
        }
      }
      // record does not carry schema. Validate each field
      Schema schema = getRecordSchema();
      Record record = (Record) value;
      for (String fieldName : getRecordSchema().getFields()) {
        FieldType fieldType = schema.getType(fieldName);
        Object fieldValue =
          fieldType.equals(FieldType.INT) ? record.getInt(fieldName, schema)
            : fieldType.equals(FieldType.LONG) ? record.getLong(fieldName, schema)
            : fieldType.equals(FieldType.FLOAT) ? record.getFloat(fieldName, schema)
            : fieldType.equals(FieldType.DOUBLE) ? record.getDouble(fieldName, schema)
            : fieldType.equals(FieldType.BOOLEAN) ? record.getBoolean(fieldName, schema)
            : fieldType.equals(FieldType.STRING) ? record.getString(fieldName, schema)
            : fieldType.equals(FieldType.BINARY) ? record.getBinary(fieldName, schema)
            : fieldType.isList() ? record.getList(fieldName, schema)
            : fieldType.isMap() ? record.getMap(fieldName, schema)
            : fieldType.isRecord() ? record.getRecord(fieldName, schema)
            : null;
        // TODO this is not needed for scalar fields
        fieldType.validateType(fieldValue, validate);
      }
    }
  }
}
