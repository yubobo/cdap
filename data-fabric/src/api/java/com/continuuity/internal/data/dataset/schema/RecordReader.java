package com.continuuity.internal.data.dataset.schema;

import com.continuuity.api.data.batch.Split;

/**
 * Defines a reader of a dataset {@link Split} as records.
 */
public abstract class RecordReader {

  /**
   * Called once at initialization.
   * @param split The split that defines the range of records to read.
   * @throws InterruptedException
   */
  public abstract void initialize(Split split) throws InterruptedException;

  /**
   * Read the next record.
   * @return true if a record was read.
   * @throws InterruptedException
   */
  public abstract boolean next() throws InterruptedException;

  /**
   * Get the current record.
   * @return The current record, or null if there is no current record.
   * @throws InterruptedException
   */
  public abstract Record getCurrent() throws InterruptedException;

  /**
   * The current progress of the record reader through its data.
   * In the default implementation, progress is not reported in the middle of split reading.
   * @return A number between 0.0 and 1.0 that is the fraction of the data that has been read.
   * @throws InterruptedException
   */
  public float getProgress() throws InterruptedException {
    return 0;
  }

  /**
   * Close the record reader.
   */
  public abstract void close();
}
