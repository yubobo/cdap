package com.continuuity.hive;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;

import java.util.List;

/**
 *
 */
public class ReflectionObjectInspector extends StructObjectInspector {
  @Override
  public List<? extends StructField> getAllStructFieldRefs() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public StructField getStructFieldRef(String s) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Object getStructFieldData(Object o, StructField structField) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public List<Object> getStructFieldsDataAsList(Object o) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getTypeName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Category getCategory() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
