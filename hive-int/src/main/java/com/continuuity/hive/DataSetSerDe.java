package com.continuuity.hive;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde2.AbstractSerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.io.Writable;

import java.util.Properties;

/**
 * Hive SerDe implementation for reading and writing datasets.
 */
public class DataSetSerDe extends AbstractSerDe {
  @Override
  public void initialize(Configuration entries, Properties properties) throws SerDeException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Class<? extends Writable> getSerializedClass() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Writable serialize(Object o, ObjectInspector objectInspector) throws SerDeException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public SerDeStats getSerDeStats() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Object deserialize(Writable writable) throws SerDeException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ObjectInspector getObjectInspector() throws SerDeException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
