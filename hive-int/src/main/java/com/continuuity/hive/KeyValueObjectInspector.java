package com.continuuity.hive;

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import java.util.List;

/**
 *
 */
public class KeyValueObjectInspector extends BaseStructObjectInspector {
  private static Log LOG = LogFactory.getLog(KeyValueObjectInspector.class);

  public KeyValueObjectInspector(List<String> columns, List<TypeInfo> columnTypes, String rowKeyField) {
    super(columns, columnTypes, rowKeyField);
  }

  @Override
  public Object getStructFieldData(Object object, StructField structField) {
    if (structField.getFieldName().equals(rowKeyField)) {
      return convertTypeIfNeeded(((KeyValue) object).getKey(), structField.getFieldObjectInspector());
    }
    return convertTypeIfNeeded(((KeyValue) object).getValue(), structField.getFieldObjectInspector());
  }

  @Override
  public List<Object> getStructFieldsDataAsList(Object object) {
    List<Object> data = Lists.newArrayListWithCapacity(2);
    data.add(((KeyValue) object).getKey());
    data.add(((KeyValue) object).getValue());
    return data;
  }

  private Object convertTypeIfNeeded(Object fieldValue, ObjectInspector fieldObjectInspector) {
    if (fieldValue == null) {
      return null;
    }
    LOG.info("Field value type is " + fieldValue.getClass());
    LOG.info("Field OI is " + fieldObjectInspector);
    ObjectInspector actualInspector =
      PrimitiveObjectInspectorFactory.getPrimitiveObjectInspectorFromClass(fieldValue.getClass());
    LOG.info("Actual type OI is " + actualInspector);
    LOG.info("Actual OI primitive category is " + ((PrimitiveObjectInspector) actualInspector).getPrimitiveCategory());
    ObjectInspectorConverters.Converter converter =
      ObjectInspectorConverters.getConverter(actualInspector, fieldObjectInspector);
    return converter.convert(fieldValue);
  }
}
