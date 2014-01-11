package com.continuuity.hive;

import com.continuuity.api.data.dataset.table.Row;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.ListTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.MapTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.UnionTypeInfo;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class RowObjectInspector extends StructObjectInspector {
  private List<RowField> fields;
  private Map<String, RowField> fieldsByName;

  public RowObjectInspector(List<String> columns, List<TypeInfo> columnTypes) {
    if (columns.size() != columnTypes.size()) {
      throw new IllegalArgumentException("Length of column names must match length of column types");
    }

    fields = Lists.newArrayListWithCapacity(columns.size());
    fieldsByName = Maps.newHashMapWithExpectedSize(columns.size());
    for (int i = 0; i < columns.size(); i++) {
      RowField field = new RowField(columns.get(i), columnTypes.get(i));
      fields.add(field);
      fieldsByName.put(field.getFieldName(), field);
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

  @Override
  public Object getStructFieldData(Object data, StructField structField) {
    if (data == null) {
      return null;
    }
    Row row = (Row) data;
    String fieldName = structField.getFieldName();
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
    for (RowField f : fields) {
      values.add(getStructFieldData(data, f));
    }
    return values;
  }

  public static ObjectInspector getInspectorForType(TypeInfo info) {
    switch (info.getCategory()) {
      case PRIMITIVE:
        PrimitiveTypeInfo pInfo = (PrimitiveTypeInfo) info;
        return ObjectInspectorFactory.getReflectionObjectInspector(pInfo.getPrimitiveJavaClass(),
                                                                   ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
      case LIST:
        ListTypeInfo lInfo = (ListTypeInfo) info;
        ObjectInspector eltInspector = getInspectorForType(lInfo.getListElementTypeInfo());
        return ObjectInspectorFactory.getStandardListObjectInspector(eltInspector);
      case MAP:
        MapTypeInfo mInfo = (MapTypeInfo) info;
        ObjectInspector keyInspector = getInspectorForType(mInfo.getMapKeyTypeInfo());
        ObjectInspector valueInspector = getInspectorForType(mInfo.getMapValueTypeInfo());
        return ObjectInspectorFactory.getStandardMapObjectInspector(keyInspector, valueInspector);
      case STRUCT:
        StructTypeInfo sInfo = (StructTypeInfo) info;
        List<String> fieldNames = sInfo.getAllStructFieldNames();
        List<TypeInfo> fieldInfos = sInfo.getAllStructFieldTypeInfos();
        List<ObjectInspector> fieldInspectors = Lists.newArrayListWithCapacity(fieldInfos.size());
        for (TypeInfo i : fieldInfos) {
          fieldInspectors.add(getInspectorForType(i));
        }
        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldInspectors);
      case UNION:
        UnionTypeInfo uInfo = (UnionTypeInfo) info;
        List<TypeInfo> allInfos = uInfo.getAllUnionObjectTypeInfos();
        List<ObjectInspector> allInspectors = Lists.newArrayListWithCapacity(allInfos.size());
        for (TypeInfo i : allInfos) {
          allInspectors.add(getInspectorForType(i));
        }
        return ObjectInspectorFactory.getStandardUnionObjectInspector(allInspectors);
      default:
        // should never get here
        throw new IllegalArgumentException("Unknown TypeInfo provided " + info.toString());
    }
  }

  private static class RowField implements StructField {
    private String name;
    private ObjectInspector fieldInspector;

    public RowField(String name, TypeInfo fieldType) {
      this.name = name;
      this.fieldInspector = getInspectorForType(fieldType);
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
}
