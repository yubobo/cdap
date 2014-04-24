package com.continuuity.internal.data.dataset.schema;

import com.continuuity.common.utils.ImmutablePair;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of a record that carries a schema but does not serialize it. Also,
 * fields are stored without their names, and the schema is required to retrieve the fields.
 * When retrieving, the requested type is not validated against the schema, under the assumption
 * that the caller knows the schema and requests the correct type. However, if a request is made
 * with an incorrect type, it most like will still fail with a ClassCastException.
 *
 * This class also provides a Builder. The builder can optionally validate the fields of the
 * record as they are being set. See {@link Validate}.
 */
public class DefaultRecord implements Record {

  private final Object[] fields;
  private final Schema schema;

  private DefaultRecord(Object[] fields, Schema schema) {
    this.fields = fields;
    this.schema = schema;
  }

  private Object getField(String fieldName, Schema schema) {
    return fields[schema.getPosition(fieldName)];
  }

  @Override
  public Integer getInt(String fieldName, Schema schema) {
    return (Integer) getField(fieldName, schema);
  }

  @Override
  public Long getLong(String fieldName, Schema schema) {
    return (Long) getField(fieldName, schema);
  }

  @Override
  public Boolean getBoolean(String fieldName, Schema schema) {
    return (Boolean) getField(fieldName, schema);
  }

  @Override
  public Float getFloat(String fieldName, Schema schema) {
    return (Float) getField(fieldName, schema);
  }

  @Override
  public Double getDouble(String fieldName, Schema schema) {
    return (Double) getField(fieldName, schema);
  }

  @Override
  public String getString(String fieldName, Schema schema) {
    return (String) getField(fieldName, schema);
  }

  @Override
  public byte[] getBinary(String fieldName, Schema schema) {
    return (byte[]) getField(fieldName, schema);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> getList(String fieldName, Schema schema) {
    return (List<T>) getField(fieldName, schema);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V> Map<K, V> getMap(String fieldName, Schema schema) {
    return (Map<K, V>) getField(fieldName, schema);
  }

  @Override
  public Record getRecord(String fieldName, Schema schema) {
    return (Record) getField(fieldName, schema);
  }

  // package visible for FieldType.validate()
  Schema getSchema() {
    return schema;
  }

  /**
   * @return a builder for the given schema with default validation.
   */
  public static Builder of(Schema schema) {
    return new Builder(schema);
  }

  /**
   * @return a builder for the given schema with the given validation.
   */
  public static Builder of(Schema schema, Validate validate) {
    return new Builder(schema, validate);
  }

  /**
   * A builder for a record that validates the schema as fields are added.
   */
  public static class Builder {

    private final Validate validate;
    private final Schema schema;
    private final Object[] fields;

    private int validate(String fieldName, FieldType valueType) {
      ImmutablePair<Integer, FieldType> entry = schema.getPositionAndType(fieldName);
      if (validate != Validate.OFF && !valueType.equals(entry.getSecond())) {
        throw new IllegalArgumentException(
          "invalid type " + valueType + " for field '" + fieldName + "', expected " + entry.getSecond());
      }
      return entry.getFirst();
    }

    private int validateType(String fieldName, Object value) {
      ImmutablePair<Integer, FieldType> entry = schema.getPositionAndType(fieldName);
      entry.getSecond().validateType(value, validate);
      return entry.getFirst();
    }

    private Builder(Schema schema) {
      this(schema, Validate.RELAXED);
    }

    private Builder(Schema schema, Validate validate) {
      this.validate = validate;
      this.schema = schema;
      this.fields = new Object[schema.getFields().size()];
    }

    public Builder set(String fieldName, Integer value) {
      fields[validate(fieldName, FieldType.INT)] = value;
      return this;
    }

    public Builder set(String fieldName, Long value) {
      fields[validate(fieldName, FieldType.LONG)] = value;
      return this;
    }

    public Builder set(String fieldName, Float value) {
      fields[validate(fieldName, FieldType.FLOAT)] = value;
      return this;
    }

    public Builder set(String fieldName, Boolean value) {
      fields[validate(fieldName, FieldType.BOOLEAN)] = value;
      return this;
    }

    public Builder set(String fieldName, Double value) {
      fields[validate(fieldName, FieldType.DOUBLE)] = value;
      return this;
    }

    public Builder set(String fieldName, String value) {
      fields[validate(fieldName, FieldType.STRING)] = value;
      return this;
    }

    public Builder set(String fieldName, byte[] value) {
      fields[validate(fieldName, FieldType.BINARY)] = value;
      return this;
    }

    public Builder set(String fieldName, List<?> value) {
      fields[validateType(fieldName, value)] = value;
      return this;
    }

    public Builder set(String fieldName, Map<?, ?> value) {
      fields[validateType(fieldName, value)] = value;
      return this;
    }

    public Builder set(String fieldName, Record value) {
      fields[validateType(fieldName, value)] = value;
      return this;
    }

    public Record build() {
      return new DefaultRecord(fields, schema);
    }
  }
}
