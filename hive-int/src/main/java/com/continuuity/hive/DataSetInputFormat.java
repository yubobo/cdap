package com.continuuity.hive;

import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.batch.BatchReadable;
import com.continuuity.api.data.batch.Split;
import com.continuuity.api.data.batch.SplitReader;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Map reduce InputFormat for reading from data sets.
 * @param <K> Class type of record keys.
 * @param <V> Class type of record values.
 */
public class DataSetInputFormat<K, V> implements InputFormat<K, V> {
  @Override
  public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
    // 1. retrieve dataset metadata

    // 2. get a dataset instance

    // 3. delegate to BatchReadable.getSplits()
    return new InputSplit[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public RecordReader<K, V> getRecordReader(InputSplit split, JobConf job, Reporter reporter) throws IOException {
    // get the dataset instance
    DataSet dataSet = null;
    // TODO: how to instantiate???

    // instantiate a new record reader for the given split
    if (dataSet instanceof BatchReadable) {
      if (split instanceof DataSetInputSplit) {
        DataSetInputSplit dataSetSplit = (DataSetInputSplit) split;
        return new DataSetRecordReader(dataSetSplit,
                                       ((BatchReadable)dataSet).createSplitReader(dataSetSplit.getDataSetSplit()));
      } else {
        throw new IOException("Invalid type for InputSplit: " + split.getClass().getName());
      }
    } else {
      throw new IOException("DataSet instance must implement BatchReadable!");
    }
  }

  /**
   * TODO: implement
   */
  public static class DataSetInputSplit implements InputSplit {
    private final Split dataSetSplit;

    public DataSetInputSplit(Split dataSetSplit) {
      this.dataSetSplit = dataSetSplit;
    }

    public Split getDataSetSplit() {
      return dataSetSplit;
    }

    @Override
    public long getLength() throws IOException {
      return dataSetSplit.getLength();
    }

    @Override
    public String[] getLocations() throws IOException {
      // not currently exposed by BatchReadable
      return new String[0];
    }

    @Override
    public void write(DataOutput out) throws IOException {
      // TODO: implement with extra support in Split (needs to be serializable)
    }

    @Override
    public void readFields(DataInput in) throws IOException {
      // TODO: implement with extra support in Split (needs to be serializable)
    }
  }

  /**
   * TODO: implement
   */
  public static class DataSetRecordReader implements RecordReader {
    private final DataSetInputSplit split;
    private final SplitReader splitReader;

    private boolean initialized = false;

    public DataSetRecordReader(DataSetInputSplit split, SplitReader splitReader) {
      this.split = split;
      this.splitReader = splitReader;
    }

    private void initialize() throws IOException {
      try {
        // TODO: is this necessary?
        splitReader.initialize(split.getDataSetSplit());
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        throw new IOException("Interrupted while initializing reader", ie);
      }
      initialized = true;
    }

    @Override
    public boolean next(Object key, Object value) throws IOException {
      if (!initialized) {
        initialize();
      }

      try {
        return splitReader.nextKeyValue();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        throw new IOException("Interrupted while retrieving the next key/value", ie);
      }
    }

    @Override
    public Object createKey() {
      // not used?
      return null;
    }

    @Override
    public Object createValue() {
      // not used?
      return null;
    }

    @Override
    public long getPos() throws IOException {
      // TODO: implement
      return 0;
    }

    @Override
    public void close() throws IOException {
      splitReader.close();
    }

    @Override
    public float getProgress() throws IOException {
      try {
        return splitReader.getProgress();
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        throw new IOException("Interrupted while getting progress", ie);
      }
    }
  }
}
