package com.continuuity.hive;

import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.DataSetSpecification;
import com.continuuity.api.data.batch.HiveReadable;
import com.continuuity.api.data.dataset.table.Row;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde2.AbstractSerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.typeinfo.ListTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.MapTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.UnionTypeInfo;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Writable;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Hive SerDe implementation for reading and writing datasets.
 *
 * <p>The initial implemention is subject to the following constraints:
 * <ul>
 *   <li>For data sets that return the {@code Row} interface, the column names and types are taken from the
 *   table definition in Hive.</li>
 *   <li>For data sets that return complex Java objects, introspection will be used to match field names to
 *   the class' instance fields.</li>
 * </ul>
 * </p>
 */
public class DataSetSerDe extends AbstractSerDe {
  private static Log LOG = LogFactory.getLog(DataSetSerDe.class);

  /** Property key for the reactor account owning the dataset to query. */
  public static final String DATASET_ACCOUNT_KEY = "reactor_account";
  /** Property key for the dataset name to query. */
  public static final String DATASET_NAME_KEY = "reactor_dataset";
  /** Property key for the table name in the dataset to query. */
  public static final String DATASET_TABLE_KEY = "dataset_table";
  /** Property key for the field name used to store the rowkey. */
  public static final String ROWKEY_FIELD = "continuuity.rowkey.fieldname";

  private Class<?> valueType;

  private LazySimpleSerDe.SerDeParameters serdeParams;

  private String rowKeyField;

  /**
   * Initialize the serde based on the configured table properties.  Reactor datasets can be accessed by creating
   * "external" tables in Hive, and using the table properties to specify all three necessary values for looking
   * up the dataset definition:
   * <ul>
   *   <li>reactor_account - the account name who owns the dataset</li>
   *   <li>reactor_dataset - the dataset name</li>
   *   <li>dataset_table - the table name from the dataset</li>
   * </ul>
   * @param conf the system configuration.
   * @param tableProps the set of properties configured for the table definition.
   * @throws SerDeException if an error occurs while reading the data set definition.
   */
  @Override
  public void initialize(Configuration conf, Properties tableProps) throws SerDeException {

    serdeParams = LazySimpleSerDe.initSerdeParams(conf, tableProps, getClass().getName());
    LOG.info("Column names are " + serdeParams.getColumnNames());
    LOG.info("Column types are " + serdeParams.getColumnTypes());
    logProperties("Table properties are:", tableProps);

    // obtain dataset name and table name from properties
    String accountName = tableProps.getProperty(DATASET_ACCOUNT_KEY);
    String datasetName = tableProps.getProperty(DATASET_NAME_KEY);
    LOG.info("Account name is " + accountName + ";  Dataset name is " + datasetName);
//    String tableName = properties.getProperty(DATASET_TABLE_KEY);
    rowKeyField = tableProps.getProperty(ROWKEY_FIELD);

    // obtain dataset instance
    DataSetUtil dsUtil = new DataSetUtil(conf);
    try {
      DataSetSpecification datasetSpec = dsUtil.getDataSetSpecification(conf, accountName, datasetName);
      if (datasetSpec == null) {
        throw new SerDeException("No dataset found for account: " + accountName + ", dataset: " + datasetName);
      }
      DataSet dataset = dsUtil.getDataSetInstance(conf, datasetSpec);
      if (!(dataset instanceof HiveReadable)) {
        throw new SerDeException("DataSet " + datasetName + " must implement HiveReadable interface");
      }
      // get schema from dataset table definition
      HiveReadable readableDS = (HiveReadable) dataset;
      valueType = readableDS.getValueType();
      LOG.info("Value type for dataset " + datasetName + " is " + valueType.getName());
//      for (DataSetTable t : readableDS.getSchema().listTables()) {
//        if (t.getName().equals(tableName)) {
//          tableCols = t.getDataColumns();
//          break;
//        }
//      }
    } catch (IOException ioe) {
      throw new SerDeException("Error obtaining Reactor dataset", ioe);
    }
  }

  private void logProperties(String message, Properties props) {
    LOG.info(message);
    for (String name : props.stringPropertyNames()) {
      LOG.info(name + "=" + props.getProperty(name));
    }
  }

  @Override
  public Class<? extends Writable> getSerializedClass() {
    return ObjectWritable.class;
  }

  @Override
  public Writable serialize(Object o, ObjectInspector objectInspector) throws SerDeException {
    return new ObjectWritable(o);
  }

  @Override
  public SerDeStats getSerDeStats() {
    // TODO: track and return real values for datasets
    // this would require tracking (or approximating) row count for datasets
    return new SerDeStats();
  }

  @Override
  public Object deserialize(Writable writable) throws SerDeException {
    KeyedObjectWritable keyedWritable = (KeyedObjectWritable) writable;
    Object value = keyedWritable.get();
    LOG.info("Deserialized value is " + (value == null ? "null" : value.getClass().getName()));
    if (value.getClass().isArray() || value.getClass().isPrimitive()) {
      LOG.info("Deserializing as KeyValue");
      return new KeyValue(keyedWritable.getKey(), value);
    } else {
      return value;
    }
  }

  @Override
  public ObjectInspector getObjectInspector() throws SerDeException {
    if (Row.class.isAssignableFrom(valueType)) {
      return new RowObjectInspector(serdeParams.getColumnNames(), serdeParams.getColumnTypes(), rowKeyField);
    } else if (KeyValue.class.isAssignableFrom(valueType) || valueType.isArray() || valueType.isPrimitive()) {
      return new KeyValueObjectInspector(serdeParams.getColumnNames(), serdeParams.getColumnTypes(), rowKeyField);
    } else {
      // return a new reflection inspector based on the given type, using the defined table columns
      return ObjectInspectorFactory.getReflectionObjectInspector(valueType,
                                                                 ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
    }
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
}
