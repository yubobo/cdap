package com.continuuity.data2.transaction.persist;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data2.transaction.TxConstants;
import com.continuuity.data2.transaction.snapshot.SnapshotCodecProvider;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

/**
 * {@link com.continuuity.data2.transaction.persist.TransactionStateStorage} implementation that provides read-only
 * access to any previously written snapshots.  This is useful for tools such as HBase {@code TransactionDataJanitor}
 * coprocessors, where we want to read snapshots, but should not have write access.
 *
 * The directory used for file storage is configured using the {@code data.tx.snapshot.dir} configuration property.
 * Both snapshot and transaction log files are suffixed with a timestamp to allow easy ordering.  Snapshot files
 * are written with the filename "snapshot.&lt;timestamp&gt;".  Transaction log files are written with the filename
 * "txlog.&lt;timestamp&gt;".
 */
public class HDFSReadOnlyTransactionStateStorage extends AbstractTransactionStateStorage {

  protected static final Logger LOG = LoggerFactory.getLogger(HDFSTransactionStateStorage.class);
  protected static final String SNAPSHOT_FILE_PREFIX = "snapshot.";
  protected static final String TMP_SNAPSHOT_FILE_PREFIX = ".in-progress.snapshot.";
  protected static final String LOG_FILE_PREFIX = "txlog.";
  private static final PathFilter SNAPSHOT_FILE_FILTER = new PathFilter() {
    @Override
    public boolean accept(Path path) {
      return path.getName().startsWith(SNAPSHOT_FILE_PREFIX);
    }
  };

  // buffer size used for HDFS reads and writes
  protected static final int BUFFER_SIZE = 16384;

  protected CConfiguration conf;
  protected FileSystem fs;
  protected Configuration hConf;
  protected String configuredSnapshotDir;
  protected Path snapshotDir;

  public HDFSReadOnlyTransactionStateStorage(CConfiguration config, Configuration hConf,
                                             SnapshotCodecProvider codecProvider) {
    super(codecProvider);
    configuredSnapshotDir = config.get(TxConstants.Manager.CFG_TX_SNAPSHOT_DIR);
    this.hConf = hConf;
    this.conf = config;
  }

  @Override
  protected void startUp() throws Exception {
    Preconditions.checkState(configuredSnapshotDir != null, "Snapshot directory is not configured.  Please set " +
                               TxConstants.Manager.CFG_TX_SNAPSHOT_DIR + " in configuration.");
    String hdfsUser = conf.get(Constants.CFG_HDFS_USER);
    if (hdfsUser == null || UserGroupInformation.isSecurityEnabled()) {
      if (hdfsUser != null && LOG.isDebugEnabled()) {
        LOG.debug("Ignoring configuration {}={}, running on secure Hadoop", Constants.CFG_HDFS_USER, hdfsUser);
      }
      // NOTE: we can start multiple times this storage. As hdfs uses per-jvm cache, we want to create new fs instead
      //       of getting closed one
      fs = FileSystem.newInstance(FileSystem.getDefaultUri(hConf), hConf);
    } else {
      fs = FileSystem.newInstance(FileSystem.getDefaultUri(hConf), hConf, hdfsUser);
    }
    snapshotDir = new Path(configuredSnapshotDir);
    LOG.info("Using snapshot dir " + snapshotDir);
  }

  @Override
  protected void shutDown() throws Exception {
    fs.close();
  }

  @Override
  public TransactionSnapshot getLatestSnapshot() throws IOException {
    InputStream in = getLatestSnapshotInputStream();
    if (in == null) {
      return null;
    }
    try {
      return readSnapshotInputStream(in);
    } finally {
      in.close();
    }
  }

  private InputStream getLatestSnapshotInputStream() throws IOException {
    TimestampedFilename[] snapshots = listSnapshotFiles();
    Arrays.sort(snapshots);
    if (snapshots.length > 0) {
      // last is the most recent
      return fs.open(snapshots[snapshots.length - 1].getPath(), HDFSTransactionStateStorage.BUFFER_SIZE);
    }

    LOG.info("No snapshot files found in {}", snapshotDir);
    return null;
  }

  private TransactionSnapshot readSnapshotInputStream(InputStream in) throws IOException {
    return codecProvider.decode(in);
  }

  protected TransactionSnapshot readSnapshotFile(Path filePath) throws IOException {
    FSDataInputStream in = fs.open(filePath, HDFSTransactionStateStorage.BUFFER_SIZE);
    try {
      return readSnapshotInputStream(in);
    } finally {
      in.close();
    }
  }

  protected TimestampedFilename[] listSnapshotFiles() throws IOException {
    FileStatus[] snapshotFileStatuses = fs.listStatus(snapshotDir, SNAPSHOT_FILE_FILTER);
    TimestampedFilename[] snapshotFiles = new TimestampedFilename[snapshotFileStatuses.length];
    for (int i = 0; i < snapshotFileStatuses.length; i++) {
      snapshotFiles[i] = new TimestampedFilename(snapshotFileStatuses[i].getPath());
    }
    return snapshotFiles;
  }

  @Override
  public List<String> listSnapshots() throws IOException {
    FileStatus[] files = fs.listStatus(snapshotDir, SNAPSHOT_FILE_FILTER);
    return Lists.transform(Arrays.asList(files), new Function<FileStatus, String>() {
      @Nullable
      @Override
      public String apply(@Nullable FileStatus input) {
        return input.getPath().getName();
      }
    });
  }

  @Override
  public List<String> listLogs() throws IOException {
    FileStatus[] files = fs.listStatus(snapshotDir, new LogFileFilter(0, Long.MAX_VALUE));
    return Lists.transform(Arrays.asList(files), new Function<FileStatus, String>() {
      @Nullable
      @Override
      public String apply(@Nullable FileStatus input) {
        return input.getPath().getName();
      }
    });
  }

  @Override
  public String getLocation() {
    return snapshotDir.toString();
  }

  protected TransactionLog openLog(Path path, long timestamp) {
    return new HDFSTransactionLog(conf, fs, hConf, path, timestamp);
  }

  @Override
  public List<TransactionLog> getLogsSince(long timestamp) throws IOException {
    FileStatus[] statuses = fs.listStatus(snapshotDir, new LogFileFilter(timestamp, Long.MAX_VALUE));
    TimestampedFilename[] timestampedFiles = new TimestampedFilename[statuses.length];
    for (int i = 0; i < statuses.length; i++) {
      timestampedFiles[i] = new TimestampedFilename(statuses[i].getPath());
    }
    return Lists.transform(Arrays.asList(timestampedFiles), new Function<TimestampedFilename, TransactionLog>() {
      @Nullable
      @Override
      public TransactionLog apply(@Nullable TimestampedFilename input) {
        return openLog(input.getPath(), input.getTimestamp());
      }
    });
  }

  protected static class LogFileFilter implements PathFilter {
    // starting time of files to include (inclusive)
    private final long startTime;
    // ending time of files to include (exclusive)
    private final long endTime;

    public LogFileFilter(long startTime, long endTime) {
      this.startTime = startTime;
      this.endTime = endTime;
    }

    @Override
    public boolean accept(Path path) {
      if (path.getName().startsWith(LOG_FILE_PREFIX)) {
        String[] parts = path.getName().split("\\.");
        if (parts.length == 2) {
          try {
            long fileTime = Long.parseLong(parts[1]);
            return fileTime >= startTime && fileTime < endTime;
          } catch (NumberFormatException ignored) {
            LOG.warn("Filename {} did not match the expected pattern prefix.<timestamp>", path.getName());
          }
        }
      }
      return false;
    }
  }

  /**
   * Represents a filename composed of a prefix and a ".timestamp" suffix.  This is useful for manipulating both
   * snapshot and transaction log filenames.
   */
  protected static class TimestampedFilename implements Comparable<HDFSTransactionStateStorage.TimestampedFilename> {
    private Path path;
    private String prefix;
    private long timestamp;

    public TimestampedFilename(Path path) {
      this.path = path;
      String[] parts = path.getName().split("\\.");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Filename " + path.getName() +
            " did not match the expected pattern prefix.timestamp");
      }
      prefix = parts[0];
      timestamp = Long.parseLong(parts[1]);
    }

    public Path getPath() {
      return path;
    }

    public String getPrefix() {
      return prefix;
    }

    public long getTimestamp() {
      return timestamp;
    }

    @Override
    public int compareTo(TimestampedFilename other) {
      int res = prefix.compareTo(other.getPrefix());
      if (res == 0) {
        res = Longs.compare(timestamp, other.getTimestamp());
      }
      return res;
    }
  }
}
