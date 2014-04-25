package com.continuuity.internal.data.dataset.schema;

import com.continuuity.common.utils.ImmutablePair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Represents the schema of a record or a dataset.
 */
public final class Schema {

  private final List<String> names;
  private final Map<String, ImmutablePair<Integer, FieldType>> fields;

  /**
   * @param fieldName name of the field
   * @return the type of that field.
   * @throws java.lang.IllegalArgumentException if the field does not exist. Use {@link #hasField(String)} to find
   * out whether a field exists.
   */
  public FieldType getType(String fieldName) {
    ImmutablePair<Integer, FieldType> entry = fields.get(fieldName);
    if (entry == null) {
      throw new IllegalArgumentException("field '" + fieldName + "' does not exist in schema");
    }
    return entry.getSecond();
  }

  /**
   * @param fieldName name of the field
   * @return the psotion of that field.
   * @throws java.lang.IllegalArgumentException if the field does not exist. Use {@link #hasField(String)} to find
   * out whether a field exists.
   */
  public Integer getPosition(String fieldName) {
    ImmutablePair<Integer, FieldType> entry = fields.get(fieldName);
    if (entry == null) {
      throw new IllegalArgumentException("field '" + fieldName + "' does not exist in schema");
    }
    return entry.getFirst();
  }

  /**
   * @param fieldName name of the field
   * @return a pair consisting oi the position and the type of that field.
   * @throws java.lang.IllegalArgumentException if the field does not exist. Use {@link #hasField(String)} to find
   * out whether a field exists.
   */
  public ImmutablePair<Integer, FieldType> getPositionAndType(String fieldName) {
    ImmutablePair<Integer, FieldType> entry = fields.get(fieldName);
    if (entry == null) {
      throw new IllegalArgumentException("field '" + fieldName + "' does not exist in schema");
    }
    return entry;
  }

  /**
   * @param fieldName name of the field
   * @return whether the field exists.
   */
  public boolean hasField(String fieldName) {
    return fields.containsKey(fieldName);
  }

  /**
   * @return The ordered list of fields of this schema.
   */
  public List<String> getFields() {
    return names;
  }

  private Schema(Map<String, ImmutablePair<Integer, FieldType>> fields, List<String> names) {
    this.fields = ImmutableMap.copyOf(fields);
    this.names = ImmutableList.copyOf(names);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }
    return fields.equals(((Schema) other).fields);
  }

  @Override
  public int hashCode() {
    return fields.hashCode();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder pattern for a Schema.
   */
  public static class Builder {

    private Map<String, ImmutablePair<Integer, FieldType>> fields = Maps.newHashMap();
    private List<String> fieldNames = Lists.newLinkedList();

    public Builder add(String name, FieldType type) {
      fields.put(name, ImmutablePair.of(fieldNames.size(), type));
      fieldNames.add(name);
      return this;
    }

    public Schema build() {
      return new Schema(fields, fieldNames);
    }
  }
}
