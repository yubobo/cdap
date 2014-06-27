package com.continuuity.examples.dataset;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.dataset.Dataset;
import com.continuuity.api.dataset.DatasetSpecification;
import com.continuuity.api.dataset.lib.AbstractDataset;
import com.continuuity.api.dataset.table.Delete;
import com.continuuity.api.dataset.table.Put;
import com.continuuity.api.dataset.table.Row;
import com.continuuity.api.dataset.table.Scanner;
import com.continuuity.api.dataset.table.Table;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides an extension to {@link Table} to allow reading data by index.
 */
public class SampleDataset extends AbstractDataset {

  private static final Logger LOG = LoggerFactory.getLogger(SampleDataset.class);
  private static final Gson GSON = new Gson();

  // Use a null value as separator for multiple field in the row key
  private static final byte NULL_BYTE = (byte) 0x0;
  private static final byte[] KEY_SEP = new byte[]{ NULL_BYTE };

  // Column qualifier used to store the origin Table row key
  private static final byte[] ROW_KEY_COL = Bytes.toBytes("r");

  // Meta key prefix used to store a record of the index rows created for each origin row
  private static final byte[] META_ROW_PREFIX = Bytes.toBytes("_meta_");

  /**
   * Map of index name -> index table (contains index value -> key)
   */
  private final Map<ByteArrayWrapper, Table> indices;
  private final Table table;
  private final Table meta;
  private final byte[] timestampField;

  public SampleDataset(DatasetSpecification spec, Table table, Table meta, Map<ByteArrayWrapper, Table> indices) {
    super(spec.getName(), ImmutableList.<Dataset>builder().add(table).addAll(indices.values()).build());
    this.table = table;
    this.meta = meta;
    this.timestampField = Bytes.toBytes(spec.getProperty("timestampField"));
    this.indices = indices;
  }

  public void deleteIndex(SampleDatasetAdmin admin, byte[] indexedColumn) throws IOException {
    indices.remove(new ByteArrayWrapper(indexedColumn));
    admin.deleteIndex(indexedColumn);
  }

  public Row get(byte[] key, byte[]... columns) {
    return table.get(key, columns);
  }

  public void put(Put put) {
    Map<byte[], byte[]> values = put.getValues();
    logValues(values);
    byte[] timestampValue = values.get(timestampField);

    // Used to add a record of index keys to the origin row, so we can handle deletes
    Put metaRow = new Put(Bytes.add(META_ROW_PREFIX, KEY_SEP, put.getRow()));
    for (Map.Entry<byte[], byte[]> entry : put.getValues().entrySet()) {
      if (Bytes.equals(entry.getKey(), timestampField) || isIgnoredIndex(entry.getKey())) {
        continue;
      }
      // Construct index key as: column (type) + value + timestamp + rowkey
      byte[] indexKey = Bytes.add(entry.getKey(), KEY_SEP,
                                  Bytes.add(entry.getValue(), KEY_SEP,
                                            Bytes.add(timestampValue, KEY_SEP, put.getRow())));
      if (LOG.isTraceEnabled()) {
        LOG.trace("Putting index row: " + Bytes.toStringBinary(indexKey));
      }
      getIndexTable(entry.getKey()).put(indexKey, ROW_KEY_COL, put.getRow());
      metaRow.add(indexKey, Bytes.EMPTY_BYTE_ARRAY);
    }
    meta.put(metaRow);
    table.put(put);
  }

  /**
   * Deletes a full row or specific columns from a row in the origin Table, plus any associated index rows.
   * @param delete The Delete request for the origin Table.
   */
  public void delete(Delete delete) {
    // Retrieve the record of any stored index rows
    byte[] metaRowKey = Bytes.add(META_ROW_PREFIX, KEY_SEP, delete.getRow());
    Row metaRow = meta.get(metaRowKey);

    List<byte[]> columnsToDelete = delete.getColumns();
    if (columnsToDelete != null && !columnsToDelete.isEmpty()) {

      // Only delete index rows matching the deleted columns
      for (byte[] col : columnsToDelete) {
        for (byte[] metaKey : metaRow.getColumns().keySet()) {
          if (Bytes.startsWith(metaKey, Bytes.add(col, KEY_SEP))) {
            getIndexTable(col).delete(metaKey);
            break;
          }
        }
      }
    } else {

      // All index rows should be deleted
      for (byte[] key : metaRow.getColumns().keySet()) {
        meta.delete(key);
      }
    }
    table.delete(delete);
  }

  /**
   * Reads a set of origin rows by a combination of required index values and a time range.
   */
  public List<Row> readBy(Map<byte[], byte[]> indexValues, long startTime, long endTime) {
    List<Row> results = Lists.newArrayList();

    // Open a separate scanner for each requested index value
    List<PeekableScanner> scanners = Lists.newArrayListWithCapacity(indexValues.size());
    for (Map.Entry<byte[], byte[]> val : indexValues.entrySet()) {
      byte[] startKey = Bytes.add(val.getKey(), KEY_SEP,
                                  Bytes.add(val.getValue(), KEY_SEP, Bytes.toBytes(startTime)));
      byte[] stopKey = Bytes.add(val.getKey(), KEY_SEP,
                                 Bytes.add(val.getValue(), KEY_SEP, Bytes.toBytes(endTime + 1)));
      LOG.debug("Scanning from " + Bytes.toStringBinary(startKey) + " to " + Bytes.toStringBinary(stopKey));
      scanners.add(new PeekableScanner(getIndexTable(val.getKey()).scan(startKey, stopKey)));
    }

    // Merge together the results
    // Only row keys that appear in all scanners are valid
    byte[] lastTimestampedRow = null;
    byte[] matchedRow = null;
    List<byte[]> matchedRowKeys = Lists.newArrayList();
    outer:
    do {
      matchedRow = null;
      for (int i = 0; i < scanners.size(); i++) {
        LOG.debug("Last timestamped row = " + Bytes.toStringBinary(lastTimestampedRow));
        PeekableScanner scanner = scanners.get(i);
        Row nextRow = scanner.peek();

        // If we hit the end of any scanner, we're done
        if (nextRow == null) {
          break outer;
        }
        if (matchedRow == null) {
          matchedRow = nextRow.get(ROW_KEY_COL);
        }
        LOG.debug("Scanner " + i + " Next row is: " + Bytes.toStringBinary(nextRow.getRow()));
        byte[] nextTimestampedRow = getTimestampedRowkey(nextRow.getRow());
        if (lastTimestampedRow == null) {
          lastTimestampedRow = nextTimestampedRow;
          continue;
        }
        int order = Bytes.compareTo(lastTimestampedRow, nextTimestampedRow);
        if (order < 0) {

          // No possible match on this scanner
          matchedRow = null;
          lastTimestampedRow = nextTimestampedRow;
          break;
        } else if (order > 0) {

          // This row doesn't exist in other scanners, so advance until we catch up
          while (Bytes.compareTo(nextTimestampedRow, lastTimestampedRow) < 0) {
            scanner.next();
            nextRow = scanner.peek();
            if (nextRow == null) {

              // Done with this scanner, no more matches
              break outer;
            }
            nextTimestampedRow = getTimestampedRowkey(nextRow.getRow());
          }
          int newOrder = Bytes.compareTo(nextTimestampedRow, lastTimestampedRow);
          if (newOrder > 0) {

            // Passed the last seen without a match
            matchedRow = null;
            lastTimestampedRow = nextTimestampedRow;
            break;
          } else if (newOrder == 0) {

            // Matched the current
            matchedRow = nextRow.get(ROW_KEY_COL);
          }
        }
      }
      if (matchedRow != null) {
        LOG.debug("Adding row " + Bytes.toStringBinary(matchedRow));
        matchedRowKeys.add(matchedRow);
        // Remove from all scanners
        for (int i = 0; i < scanners.size(); i++) {
          scanners.get(i).next();
        }
        // Reset last timestamp
        lastTimestampedRow = null;
      }
    } while (lastTimestampedRow != null || matchedRow != null);

    // Get the origin rows
    if (!matchedRowKeys.isEmpty()) {
      for (byte[] key : matchedRowKeys) {
        Row originRow = table.get(key);
        if (originRow != null) {
          results.add(originRow);
        }
      }
    }

    LOG.info("Returning " + results.size() + " results.");
    return results;
  }

  private Table getIndexTable(byte[] column) {
    byte[] key = Bytes.toBytes("idx_" + Bytes.toString(column));
    return indices.get(new ByteArrayWrapper(key));
  }

  private boolean isIgnoredIndex(byte[] column) {
    byte[] key = Bytes.toBytes("idx_" + Bytes.toString(column));
    return !indices.containsKey(new ByteArrayWrapper(key));
  }

  /**
   * Returns just the [timestamp]-[rowkey] portion of the index key.  This will be the same for index rows from the
   * same origin row.
   */
  private static byte[] getTimestampedRowkey(byte[] indexKey) {
    int sepCount = 0;
    for (int i = 0; i < indexKey.length; i++) {
      if (indexKey[i] == NULL_BYTE) {
        sepCount++;
      }
      if (sepCount > 1) {
        return Bytes.tail(indexKey, indexKey.length - (i + 1));
      }
    }
    return null;
  }

  private void logValues(Map<byte[], byte[]> values) {
    if (!LOG.isTraceEnabled()) {
      return;
    }
    LOG.trace("Put values: ");
    for (Map.Entry<byte[], byte[]> entry : values.entrySet()) {
      LOG.trace(Bytes.toStringBinary(entry.getKey()) + "=" + Bytes.toStringBinary(entry.getValue()) + ", ");
    }
  }

  private static final class PeekableScanner implements Scanner {
    private final Scanner wrapped;
    private Row nextRow;

    public PeekableScanner(Scanner toWrap) {
      this.wrapped = toWrap;
    }

    public Row peek() {
      if (nextRow == null) {
        nextRow = wrapped.next();
      }
      return nextRow;
    }

    @Override
    public Row next() {
      if (nextRow != null) {
        Row toReturn = nextRow;
        nextRow = null;
        return toReturn;
      }
      return wrapped.next();
    }

    @Override
    public void close() {
      this.wrapped.close();
      this.nextRow = null;
    }
  }
}
