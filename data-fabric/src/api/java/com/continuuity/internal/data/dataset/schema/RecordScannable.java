package com.continuuity.internal.data.dataset.schema;

import com.continuuity.api.data.batch.Split;

import java.util.List;

/**
 * This interface will be implemented by all datasets that allow scanning over records with schema.
 */
public interface RecordScannable {

  /**
   * Returns all splits of the dataset, for feeding the whole dataset into a batch job.
   * @return A list of {@link Split}s.
   */
  List<Split> getSplits();

  /**
   * Creates a reader for the split of a dataset.
   * @param split The split to create a reader for.
   * @return The instance of a {@link RecordReader}.
   */
  RecordReader createSplitReader(Split split);
}
