package com.continuuity.internal.data.dataset.schema;

import java.util.List;
import java.util.Map;

/**
 * A record is a structure with a {@link Schema}. Knowledge of the schema is outside of the record, that is,
 * the record does not embed its schema. The reason for this is that records get serialized, such as in M/R jobs,
 * and it would be very expensive to include the schema in every record.
 */
public interface Record {

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as an Integer, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as INT in the schema.
   */
  Integer getInt(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a Long, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as LONG in the schema.
   */
  Long getLong(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a Boolean, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as BOOLEAN in the schema.
   */
  Boolean getBoolean(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a Float, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as FLOAT in the schema.
   */
  Float getFloat(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a DOUBLE, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as DOUBLE in the schema.
   */
  Double getDouble(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a String, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as STRING in the schema.
   */
  String getString(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a byte array, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as BINARY in the schema.
   */
  byte[] getBinary(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a List, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as a list in the schema.
   */
  <T> List<T> getList(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a Map, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as a map in the schema.
   */
  <K, V> Map<K, V> getMap(String fieldName, Schema schema);

  /**
   * @param fieldName the name of field
   * @param schema the schema of the record
   * @return the value of the field as a Record, null if the field is not set.
   * @throws IllegalArgumentException if the field is not declared as a record in the schema.
   */
  Record getRecord(String fieldName, Schema schema);
}
