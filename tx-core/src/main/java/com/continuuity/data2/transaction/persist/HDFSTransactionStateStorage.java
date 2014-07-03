package com.continuuity.data2.transaction.persist;

import com.continuuity.common.conf.CConfiguration;

import com.continuuity.data2.transaction.snapshot.SnapshotCodecProvider;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Handles persistence of transaction snapshot and logs to a directory in HDFS.
 *
 * This implementation provides the ability to write transaction snapshots and logs.
 */
public class HDFSTransactionStateStorage extends HDFSReadOnlyTransactionStateStorage
  implements TransactionStateStorage {

  // TODO move this out as a separate command line tool
  private enum CLIMode { SNAPSHOT, TXLOG }

  @Inject
  public HDFSTransactionStateStorage(CConfiguration config, Configuration hConf,
                                     SnapshotCodecProvider codecProvider) {
    super(config, hConf, codecProvider);
  }

  @Override
  protected void startUp() throws Exception {
    super.startUp();
    if (!fs.exists(snapshotDir)) {
      LOG.info("Creating snapshot dir at {}", snapshotDir);
      fs.mkdirs(snapshotDir);
    }
  }

  @Override
  public void writeSnapshot(TransactionSnapshot snapshot) throws IOException {
    // create a temporary file, and save the snapshot
    Path snapshotTmpFile = new Path(snapshotDir, TMP_SNAPSHOT_FILE_PREFIX + snapshot.getTimestamp());
    LOG.info("Writing snapshot to temporary file {}", snapshotTmpFile);

    FSDataOutputStream out = fs.create(snapshotTmpFile, false, BUFFER_SIZE);
    // encode the snapshot and stream the serialized version to the file
    try {
      codecProvider.encode(out, snapshot);
    } finally {
      out.close();
    }

    // move the temporary file into place with the correct filename
    Path finalFile = new Path(snapshotDir, SNAPSHOT_FILE_PREFIX + snapshot.getTimestamp());
    fs.rename(snapshotTmpFile, finalFile);
    LOG.info("Completed snapshot to file {}", finalFile);
  }

  @Override
  public void writeSnapshot(OutputStream out, TransactionSnapshot snapshot) throws IOException {
    codecProvider.encode(out, snapshot);
  }

  @Override
  public long deleteOldSnapshots(int numberToKeep) throws IOException {
    TimestampedFilename[] snapshots = listSnapshotFiles();
    if (snapshots.length == 0) {
      return -1;
    }
    Arrays.sort(snapshots, Collections.reverseOrder());
    if (snapshots.length <= numberToKeep) {
      // nothing to remove, oldest timestamp is the last snapshot
      return snapshots[snapshots.length - 1].getTimestamp();
    }
    int toRemoveCount = snapshots.length - numberToKeep;
    TimestampedFilename[] toRemove = new TimestampedFilename[toRemoveCount];
    System.arraycopy(snapshots, numberToKeep, toRemove, 0, toRemoveCount);

    for (TimestampedFilename f : toRemove) {
      LOG.debug("Removing old snapshot file {}", f.getPath());
      fs.delete(f.getPath(), false);
    }
    long oldestTimestamp = snapshots[numberToKeep - 1].getTimestamp();
    LOG.info("Removed {} old snapshot files prior to {}", toRemoveCount, oldestTimestamp);
    return oldestTimestamp;
  }

  @Override
  public TransactionLog createLog(long timestamp) throws IOException {
    Path newLog = new Path(snapshotDir, LOG_FILE_PREFIX + timestamp);
    return openLog(newLog, timestamp);
  }

  @Override
  public void deleteLogsOlderThan(long timestamp) throws IOException {
    FileStatus[] statuses = fs.listStatus(snapshotDir, new LogFileFilter(0, timestamp));
    int removedCnt = 0;
    for (FileStatus status : statuses) {
      LOG.debug("Removing old transaction log {}", status.getPath());
      if (fs.delete(status.getPath(), false)) {
        removedCnt++;
      } else {
        LOG.error("Failed to delete transaction log file {}", status.getPath());
      }
    }
    LOG.info("Removed {} transaction logs older than {}", removedCnt, timestamp);
  }

  /**
   * Reads a transaction state snapshot or transaction log from HDFS and prints the entries to stdout.
   *
   * Supports the following options:
   *    -s    read snapshot state (defaults to the latest)
   *    -l    read a transaction log
   *    [filename]  reads the given file
   * @param args
   */
  public static void main(String[] args) {
    List<String> filenames = Lists.newArrayList();
    CLIMode mode = null;
    for (String arg : args) {
      if ("-s".equals(arg)) {
        mode = CLIMode.SNAPSHOT;
      } else if ("-l".equals(arg)) {
        mode = CLIMode.TXLOG;
      } else if ("-h".equals(arg)) {
        printUsage(null);
      } else {
        filenames.add(arg);
      }
    }

    if (mode == null) {
      printUsage("ERROR: Either -s or -l is required to set mode.", 1);
    }

    CConfiguration config = CConfiguration.create();

    HDFSTransactionStateStorage storage =
      new HDFSTransactionStateStorage(config, new Configuration(), new SnapshotCodecProvider(config));
    storage.startAndWait();
    try {
      switch (mode) {
        case SNAPSHOT:
          try {
            if (filenames.isEmpty()) {
              TransactionSnapshot snapshot = storage.getLatestSnapshot();
              printSnapshot(snapshot);
            }
            for (String file : filenames) {
              Path path = new Path(file);
              TransactionSnapshot snapshot = storage.readSnapshotFile(path);
              printSnapshot(snapshot);
              System.out.println();
            }
          } catch (IOException ioe) {
            System.err.println("Error reading snapshot files: " + ioe.getMessage());
            ioe.printStackTrace();
            System.exit(1);
          }
          break;
        case TXLOG:
          if (filenames.isEmpty()) {
            printUsage("ERROR: At least one transaction log filename is required!", 1);
          }
          for (String file : filenames) {
            TimestampedFilename timestampedFilename = new TimestampedFilename(new Path(file));
            TransactionLog log = storage.openLog(timestampedFilename.getPath(), timestampedFilename.getTimestamp());
            printLog(log);
            System.out.println();
          }
          break;
      }
    } finally {
      storage.stop();
    }
  }

  private static void printUsage(String message) {
    printUsage(message, 0);
  }

  private static void printUsage(String message, int exitCode) {
    if (message != null) {
      System.out.println(message);
    }
    System.out.println("Usage: java " + HDFSTransactionStateStorage.class.getName() + " (-s|-l) file1 [file2...]");
    System.out.println();
    System.out.println("\t-s\tRead files as transaction state snapshots (will default to latest if no file given)");
    System.out.println("\t-l\tRead files as transaction logs [filename is required]");
    System.out.println("\t-h\tPrint this message");
    System.exit(exitCode);
  }

  private static void printSnapshot(TransactionSnapshot snapshot) {
    Date snapshotDate = new Date(snapshot.getTimestamp());
    System.out.println("TransactionSnapshot at " + snapshotDate.toString());
    System.out.println("\t" + snapshot.toString());
  }

  private static void printLog(TransactionLog log) {
    try {
      System.out.println("TransactionLog " + log.getName());
      TransactionLogReader reader = log.getReader();
      TransactionEdit edit;
      long seq = 0;
      while ((edit = reader.next()) != null) {
        System.out.println(String.format("    %d: %s", seq++, edit.toString()));
      }
    } catch (IOException ioe) {
      System.err.println("ERROR reading log " + log.getName() + ": " + ioe.getMessage());
      ioe.printStackTrace();
    }
  }
}
