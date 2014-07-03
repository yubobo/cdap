package com.continuuity.data2.transaction.persist;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines the common contract for persisting transaction state changes.
 */
public interface TransactionStateStorage extends ReadOnlyTransactionStateStorage {

  /**
   * Persists a snapshot of transaction state to an output stream.
   */
  public void writeSnapshot(OutputStream out, TransactionSnapshot snapshot) throws IOException;

  /**
   * Persists a snapshot of transaction state.
   */
  public void writeSnapshot(TransactionSnapshot snapshot) throws IOException;

  /**
   * Removes any snapshots prior to the {@code numberToKeep} most recent.
   *
   * @param numberToKeep The number of most recent snapshots to keep.
   * @throws IOException If an error occurs while deleting old snapshots.
   * @return The timestamp of the oldest snapshot kept.
   */
  public long deleteOldSnapshots(int numberToKeep) throws IOException;

  /**
   * Creates a new {@link TransactionLog}.
   */
  public TransactionLog createLog(long timestamp) throws IOException;

  /**
   * Removes any transaction logs with a timestamp older than the given value.  Logs must be removed based on timestamp
   * to ensure we can fully recover state based on a given snapshot.
   * @param timestamp The timestamp to delete up to.  Logs with a timestamp less than this value will be removed.
   * @throws IOException If an error occurs while removing logs.
   */
  public void deleteLogsOlderThan(long timestamp) throws IOException;

}
