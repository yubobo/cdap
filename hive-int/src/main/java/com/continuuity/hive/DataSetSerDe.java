package com.continuuity.hive;

import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.batch.HiveReadable;
import com.continuuity.api.data.dataset.table.Row;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde2.AbstractSerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Writable;

import java.io.IOException;
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
  /** Configuration key for the reactor account owning the dataset to query. */
  public static final String DATASET_ACCOUNT_KEY = "reactor_account";
  /** Configuration key for the dataset name to query. */
  public static final String DATASET_NAME_KEY = "reactor_dataset";
  /** Configuration key for the table name in the dataset to query. */
  public static final String DATASET_TABLE_KEY = "dataset_table";

  private Class<?> valueType;

  private LazySimpleSerDe.SerDeParameters serdeParams;

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

    // obtain dataset name and table name from properties
    String accountName = tableProps.getProperty(DATASET_ACCOUNT_KEY);
    String datasetName = tableProps.getProperty(DATASET_NAME_KEY);
//    String tableName = properties.getProperty(DATASET_TABLE_KEY);

    // obtain dataset instance
    try {
      DataSet dataset = DataSetUtil.getDataSetInstance(conf, accountName, datasetName);
      if (!(dataset instanceof HiveReadable)) {
        throw new SerDeException("DataSet " + datasetName + " must implement HiveReadable interface");
      }
      // get schema from dataset table definition
      HiveReadable readableDS = (HiveReadable) dataset;
      valueType = readableDS.getValueType();
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
    return ((ObjectWritable) writable).get();
  }

  @Override
  public ObjectInspector getObjectInspector() throws SerDeException {
    if (Row.class.isAssignableFrom(valueType)) {
      return new RowObjectInspector(serdeParams.getColumnNames(), serdeParams.getColumnTypes());
    } else {
      // return a new reflection inspector based on the given type, using the defined table columns
      return ObjectInspectorFactory.getReflectionObjectInspector(valueType,
                                                                 ObjectInspectorFactory.ObjectInspectorOptions.JAVA);
    }
  }
}
