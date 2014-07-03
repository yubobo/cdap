package com.continuuity.data2.transaction.persist;

import com.google.common.util.concurrent.Service;

import java.io.IOException;
import java.util.List;

/**
 * Defines the common interface for reading persisted transaction state changes.
 */
public interface ReadOnlyTransactionStateStorage extends Service {

  /**
   * Returns the most recent snapshot that has been successfully written.  Note that this may return {@code null}
   * if no completed snapshot files are found.
   */
  TransactionSnapshot getLatestSnapshot() throws IOException;

  /**
   * Returns the (non-qualified) names of available snapshots.
   */
  List<String> listSnapshots() throws IOException;

  /**
   * Returns the (non-qualified) names of available logs.
   */
  List<String> listLogs() throws IOException;

  /**
   * Returns a string representation of the location used for persistence.
   */
  String getLocation();

  /**
   * Returns all {@link com.continuuity.data2.transaction.persist.TransactionLog}s with a timestamp greater than or
   * equal to the given timestamp.  Note that the returned list is guaranteed to be sorted in ascending
   * timestamp order.
   */
  List<TransactionLog> getLogsSince(long timestamp) throws IOException;
}
