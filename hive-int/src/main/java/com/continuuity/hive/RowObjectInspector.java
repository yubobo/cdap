package com.continuuity.hive;

import com.continuuity.api.data.dataset.table.Row;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public class RowObjectInspector extends BaseStructObjectInspector {
  private static final Log LOG = LogFactory.getLog(RowObjectInspector.class);

  public RowObjectInspector(List<String> columns, List<TypeInfo> columnTypes, String rowKeyField) {
    super(columns, columnTypes, rowKeyField);
  }

  @Override
  public Object getStructFieldData(Object data, StructField structField) {
    if (data == null) {
      return null;
    }
    LOG.info("Field is " + structField.getFieldName() + ", data type is " + data.getClass().getName());
    if (data.getClass().isArray()) {
      LOG.info("Array element type is " + data.getClass().getComponentType());
    }
    if (Collection.class.isAssignableFrom(data.getClass())) {
      Collection dataCol = (Collection) data;
      int idx = 0;
      for (Object elt : dataCol) {
        LOG.info("Elt #" + idx + " type is " + elt.getClass().getName());
        idx++;
      }
    }
    if (List.class.isAssignableFrom(data.getClass())) {
      List dataList = (List) data;
      for (int i = 0; i < fields.size(); i++) {
        if (fields.get(i).getFieldName().equals(structField.getFieldName())) {
          return dataList.get(i);
        }
      }
      return null;
    }

    Row row = (Row) data;
    String fieldName = structField.getFieldName();
    if (fieldName.equals(rowKeyField)) {
      return row.getRow();
    }

    // rows do not support nesting
    Category fieldCategory = structField.getFieldObjectInspector().getCategory();
    if (fieldCategory != Category.PRIMITIVE) {
      throw new IllegalArgumentException("Rows only support primitive types, but provided type is " +
                                           fieldCategory.name());
    }
    PrimitiveObjectInspector fieldInspector = (PrimitiveObjectInspector) structField.getFieldObjectInspector();
    switch (fieldInspector.getPrimitiveCategory()) {
      case VOID:
        return null;
      case BOOLEAN:
        return row.getBoolean(fieldName);
      case BYTE:
        byte[] value = row.get(fieldName);
        return value[0];
      case SHORT:
        return row.getShort(fieldName);
      case INT:
        return row.getInt(fieldName);
      case LONG:
        return row.getLong(fieldName);
      case FLOAT:
        return row.getFloat(fieldName);
      case DOUBLE:
        return row.getDouble(fieldName);
      case STRING:
      case VARCHAR:
        return row.getString(fieldName);
      case BINARY:
        return row.get(fieldName);
      case DECIMAL:
        return row.getDouble(fieldName);
      case UNKNOWN:
      case DATE:
      case TIMESTAMP:
      default:
        // not supported
        return null;
    }
  }

  @Override
  public List<Object> getStructFieldsDataAsList(Object data) {
    List values = Lists.newArrayListWithCapacity(fields.size());
    for (SimpleStructField f : fields) {
      values.add(getStructFieldData(data, f));
    }
    return values;
  }

}
