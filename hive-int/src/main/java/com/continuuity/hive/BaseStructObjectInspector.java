package com.continuuity.hive;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class BaseStructObjectInspector extends StructObjectInspector {

  protected List<SimpleStructField> fields;
  protected Map<String, SimpleStructField> fieldsByName;
  protected String rowKeyField;

  public BaseStructObjectInspector(List<String> columns, List<TypeInfo> columnTypes, String rowKeyField) {
    if (columns.size() != columnTypes.size()) {
      throw new IllegalArgumentException("Length of column names must match length of column types");
    }

    fieldsByName = Maps.newHashMapWithExpectedSize(columns.size());
    fields = Lists.newArrayListWithCapacity(columns.size());

    for (int i = 0; i < columns.size(); i++) {
      SimpleStructField field = new SimpleStructField(columns.get(i), columnTypes.get(i));
      fields.add(field);
      fieldsByName.put(field.getFieldName(), field);
    }
    if (rowKeyField != null) {
      this.rowKeyField = rowKeyField;
    } else if (columns.size() > 0) {
      // row key defaults to the first field if not specified
      this.rowKeyField = columns.get(0);
    }
  }

  @Override
  public String getTypeName() {
    return ObjectInspectorUtils.getStandardStructTypeName(this);
  }

  @Override
  public Category getCategory() {
    return Category.STRUCT;
  }

  @Override
  public List<? extends StructField> getAllStructFieldRefs() {
    return fields;
  }

  @Override
  public StructField getStructFieldRef(String s) {
    return fieldsByName.get(s);
  }
}
