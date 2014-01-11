package com.continuuity.hive;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.dataset.table.Row;
import com.continuuity.data.table.Result;
import com.google.common.collect.Maps;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class RowWritable implements Writable {
  private Row row;

  public RowWritable() {
  }

  public RowWritable(Row row) {
    this.row = row;
  }

  public Row get() {
    return row;
  }

  public void set(Row row) {
    this.row = row;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    byte[] rowkey = row.getRow();
    out.write(rowkey.length);
    out.write(rowkey);
    out.write(row.getColumns().size());
    for (Map.Entry<byte[], byte[]> e : row.getColumns().entrySet()) {
      out.write(e.getKey().length);
      out.write(e.getKey());
      out.write(e.getValue().length);
      out.write(e.getValue());
    }
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    int keylen = in.readInt();
    byte[] rowkey = new byte[keylen];
    in.readFully(rowkey);
    int colCnt = in.readInt();
    Map<byte[], byte[]> cols = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
    for (int i = 0; i < colCnt; i++) {
      int klen = in.readInt();
      byte[] colKey = new byte[klen];
      in.readFully(colKey);
      int vlen = in.readInt();
      byte[] colVal = new byte[vlen];
      in.readFully(colVal);
      cols.put(colKey, colVal);
    }
    this.row = new Result(rowkey, cols);
  }
}
