package com.continuuity.internal.data.dataset.schema;

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
  private final Map<String, FieldType> fields;

  /**
   * @param fieldName name of the field
   * @return the type of that field.
   * @throws java.lang.IllegalArgumentException if the field does not exist. Use {@link #hasField(String)} to find
   * out whether a field exists.
   */
  public FieldType getType(String fieldName) {
    FieldType fType = fields.get(fieldName);
    if (fType == null) {
      throw new IllegalArgumentException("field '" + fieldName + "' does not exist in schema");
    }
    return fType;
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

  private Schema(Map<String, FieldType> fields, List<String> names) {
    this.fields = ImmutableMap.copyOf(fields);
    this.names = ImmutableList.copyOf(names);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder pattern for a Schema.
   */
  public static class Builder {

    private Map<String, FieldType> fields = Maps.newHashMap();
    private List<String> fieldNames = Lists.newLinkedList();

    public Builder add(String name, FieldType type) {
      fields.put(name, type);
      fieldNames.add(name);
      return this;
    }

    public Schema build() {
      return new Schema(fields, fieldNames);
    }
  }
}
