package com.continuuity.hive;

import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.batch.BatchReadable;
import com.continuuity.api.data.batch.Split;
import com.continuuity.api.data.batch.SplitReader;
import com.continuuity.api.data.dataset.table.Row;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

/**
 * Map reduce InputFormat for reading from data sets.
 */
// TODO: eliminate use of ObjectWritable with something more efficient
public class DataSetInputFormat implements InputFormat<ImmutableBytesWritable, KeyedObjectWritable> {
  @Override
  public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
    // num splits is currently ignored

    // obtain dataset instance
    DataSet dataset = getDataSet(job);

    // delegate to BatchReadable.getSplits()
    if (!(dataset instanceof BatchReadable)) {
      throw new IOException("DataSet " + dataset.getName() + " must implement HiveReadable interface");
    }

    BatchReadable readableDS = (BatchReadable) dataset;
    List<Split> dsSplits = readableDS.getSplits();
    InputSplit[] inputSplits = new InputSplit[dsSplits.size()];
    for (int i = 0; i < dsSplits.size(); i++) {
      inputSplits[i] = new DataSetInputSplit(dsSplits.get(i));
    }
    return inputSplits;
  }

  @Override
  public RecordReader<ImmutableBytesWritable, KeyedObjectWritable> getRecordReader(
      InputSplit split, JobConf job, Reporter reporter) throws IOException {
    // get the dataset instance
    DataSet dataSet = getDataSet(job);

    // instantiate a new record reader for the given split
    if (dataSet instanceof BatchReadable) {
      if (split instanceof DataSetInputSplit) {
        DataSetInputSplit dataSetSplit = (DataSetInputSplit) split;
        return new DataSetRecordReader(dataSetSplit,
                                       ((BatchReadable) dataSet).createSplitReader(dataSetSplit.getDataSetSplit()));
      } else {
        throw new IOException("Invalid type for InputSplit: " + split.getClass().getName());
      }
    } else {
      throw new IOException("DataSet instance must implement BatchReadable!");
    }
  }

  private DataSet getDataSet(JobConf job) throws IOException {
    // obtain dataset name and table name from properties
    String accountName = job.get(DataSetSerDe.DATASET_ACCOUNT_KEY);
    String datasetName = job.get(DataSetSerDe.DATASET_NAME_KEY);

    // obtain dataset instance
    return DataSetUtil.getDataSetInstance(job, accountName, datasetName);
  }

  /**
   * This class duplicates all the functionality of
   * {@link com.continuuity.internal.app.runtime.batch.dataset.DataSetInputSplit}, but implements
   * {@link org.apache.hadoop.mapred.InputSplit} instead of {@link org.apache.hadoop.mapreduce.InputSplit}.
   */
  public static class DataSetInputSplit implements InputSplit {
    private Split dataSetSplit;

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
      Text.writeString(out, dataSetSplit.getClass().getName());
      String ser = new Gson().toJson(dataSetSplit);
      Text.writeString(out, ser);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
      try {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
          classLoader = getClass().getClassLoader();
        }
        Class<? extends Split> splitClass = (Class<Split>) classLoader.loadClass(Text.readString(in));
        dataSetSplit = new Gson().fromJson(Text.readString(in), splitClass);
      } catch (ClassNotFoundException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  /**
   *
   */
  public class DataSetRecordReader implements RecordReader<ImmutableBytesWritable, KeyedObjectWritable> {
    private final DataSetInputSplit split;
    private final SplitReader<byte[], Object> splitReader;

    private boolean initialized = false;

    public DataSetRecordReader(DataSetInputSplit split,
                               SplitReader<byte[], Object> splitReader) {
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
    public boolean next(ImmutableBytesWritable key, KeyedObjectWritable value) throws IOException {
      if (!initialized) {
        initialize();
      }

      boolean hasNext;
      try {
        hasNext = splitReader.nextKeyValue();
        byte[] keyBytes = splitReader.getCurrentKey();
        key.set(keyBytes);
        value.setKey(keyBytes);
        Object readerValue = splitReader.getCurrentValue();
        if (readerValue instanceof Row) {
          value.set(new RowWritable((Row) readerValue));
        } else {
          value.set(readerValue);
        }
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        throw new IOException("Interrupted while retrieving the next key/value", ie);
      }
      return hasNext;
    }

    @Override
    public ImmutableBytesWritable createKey() {
      return new ImmutableBytesWritable();
    }

    @Override
    public KeyedObjectWritable createValue() {
      return new KeyedObjectWritable();
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
