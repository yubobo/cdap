package com.continuuity.examples.dataset;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.dataset.table.Put;
import com.continuuity.api.dataset.table.Row;
import com.continuuity.data2.transaction.TransactionExecutor;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class SampleDatasetTest extends AbstractDatasetTest {

  private static final byte[] TIMESTAMP_COL = Bytes.toBytes("ts");
  private static final byte[] COLOR_COL = Bytes.toBytes("c");
  private static final byte[] TEMPERATURE_COL = Bytes.toBytes("t");

  private static Random random = new Random();

  @Before
  public void setUp() throws Exception {
    super.setUp();
    addModule("sample", new SampleDatasetModule());
  }

  @After
  public void tearDown() throws Exception {
    deleteModule("sample");
    super.tearDown();
  }

  @Test
  public void testDeleteIndex() throws Exception {
    createInstance("sample", "kvindex", SampleDatasetDefinition.properties("ts", new String[] {"c", "t"}));
    final SampleDataset table = getInstance("kvindex");
    TransactionExecutor txnl = newTransactionExecutor(table);

    long now = System.currentTimeMillis();
    final Put[] puts = new Put[] {
      createRecord(now++, "blue", "hot"),
      createRecord(now++, "blue", "cold"),
      createRecord(now++, "green", "warm")
    };

    final byte[][] rows = new byte[puts.length][];
    for (int i = 0; i < rows.length; i++) {
      rows[i] = puts[i].getRow();
    }

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        for (Put put : puts) {
          table.put(put);
        }
      }
    });

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        // blue: 1, 2
        criteria.put(COLOR_COL, Bytes.toBytes("blue"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(2, results.size());
        assertContains(results, rows[0]);
        assertContains(results, rows[1]);
      }
    });

    // delete color (c) index
    SampleDatasetAdmin admin = getAdmin("kvindex");
    table.deleteIndex(admin, COLOR_COL);

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        // blue: nothing
        criteria.put(COLOR_COL, Bytes.toBytes("blue"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(0, results.size());
      }
    });

    deleteInstance("kvindex");
  }

  @Test
  public void testKeyValueIndexing() throws Exception {
    createInstance("sample", "kvindex", SampleDatasetDefinition.properties("ts", new String[] {"c", "t"}));
    final SampleDataset table = getInstance("kvindex");
    TransactionExecutor txnl = newTransactionExecutor(table);

    long now = System.currentTimeMillis();
    final Put[] puts = new Put[] {
      createRecord(now++, "blue", "hot"),
      createRecord(now++, "blue", "cold"),
      createRecord(now++, "green", "warm"),
      createRecord(now++, "green", "hot"),
      createRecord(now++, "red", "cold"),
      createRecord(now++, "blue", "cold"),
      createRecord(now++, "blue", "hot")
    };

    final byte[][] rows = new byte[puts.length][];
    for (int i = 0; i < rows.length; i++) {
      rows[i] = puts[i].getRow();
    }

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        for (Put put : puts) {
          table.put(put);
        }
      }
    });

    /*
    Table index = table.getIndexTable();
    Scanner scan = index.scan(Bytes.EMPTY_BYTE_ARRAY, null);
    Row nextRow = null;
    int cnt = 0;
    while ((nextRow = scan.next()) != null) {
      LOG.info("Row " + cnt + ": " + Bytes.toStringBinary(nextRow.getRow()));
      cnt++;
    }
    */

    // Try some queries

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        // blue: 1, 2, 6, 7
        criteria.put(COLOR_COL, Bytes.toBytes("blue"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(4, results.size());
        assertContains(results, rows[0]);
        assertContains(results, rows[1]);
        assertContains(results, rows[5]);
        assertContains(results, rows[6]);
      }
    });

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        // green: 3, 4
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        criteria.put(COLOR_COL, Bytes.toBytes("green"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(2, results.size());
        assertContains(results, rows[2]);
        assertContains(results, rows[3]);
      }
    });

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        // cold: 2, 5, 6
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        criteria.put(TEMPERATURE_COL, Bytes.toBytes("cold"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(3, results.size());
        assertContains(results, rows[1]);
        assertContains(results, rows[4]);
        assertContains(results, rows[5]);
      }
    });

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        // hot: 1, 4, 7
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        criteria.put(TEMPERATURE_COL, Bytes.toBytes("hot"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(3, results.size());
        assertContains(results, rows[0]);
        assertContains(results, rows[3]);
        assertContains(results, rows[6]);
      }
    });

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        // blue + hot: 1, 7
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        criteria.put(COLOR_COL, Bytes.toBytes("blue"));
        criteria.put(TEMPERATURE_COL, Bytes.toBytes("hot"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(2, results.size());
        assertContains(results, rows[0]);
        assertContains(results, rows[6]);
      }
    });

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        // blue + cold: 2, 6
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        criteria.put(COLOR_COL, Bytes.toBytes("blue"));
        criteria.put(TEMPERATURE_COL, Bytes.toBytes("cold"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(2, results.size());
        assertContains(results, rows[1]);
        assertContains(results, rows[5]);
      }
    });

    txnl.execute(new TransactionExecutor.Subroutine() {
      @Override
      public void apply() throws Exception {
        // red + hot: no matches
        Map<byte[], byte[]> criteria = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
        criteria.put(COLOR_COL, Bytes.toBytes("red"));
        criteria.put(TEMPERATURE_COL, Bytes.toBytes("hot"));
        List<Row> results = table.readBy(criteria, 0, Long.MAX_VALUE - 1);
        assertEquals(0, results.size());
      }
    });

    deleteInstance("kvindex");
  }

  private Put createRecord(long timestamp, String color, String temp) {
    Put p = new Put(Bytes.toBytes(Math.abs(random.nextLong())));
    p.add(TIMESTAMP_COL, timestamp);
    p.add(COLOR_COL, color);
    p.add(TEMPERATURE_COL, temp);
    return p;
  }

  private void assertContains(List<Row> results, byte[] expectedKey) {
    for (Row r : results) {
      if (Bytes.equals(expectedKey, r.getRow())) {
        return;
      }
    }
    fail("Expected row " + Bytes.toStringBinary(expectedKey) + " not found");
  }
}
