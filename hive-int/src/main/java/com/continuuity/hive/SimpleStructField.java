package com.continuuity.hive;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

/**
*
*/
class SimpleStructField implements StructField {
  private String name;
  private ObjectInspector fieldInspector;

  public SimpleStructField(String name, TypeInfo fieldType) {
    this.name = name;
    this.fieldInspector = DataSetSerDe.getInspectorForType(fieldType);
  }

  @Override
  public String getFieldName() {
    return name;
  }

  @Override
  public ObjectInspector getFieldObjectInspector() {
    return fieldInspector;
  }

  @Override
  public String getFieldComment() {
    return "";
  }
}
