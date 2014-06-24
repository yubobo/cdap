package com.continuuity.data2.dataset2.lib.table;

import com.continuuity.api.data.batch.RecordScannable;
import com.continuuity.api.data.batch.RecordScanner;
import com.continuuity.api.data.batch.Scannables;
import com.continuuity.api.data.batch.Split;
import com.continuuity.api.dataset.lib.KeyValue;
import com.continuuity.api.dataset.lib.KeyValueTable;
import com.continuuity.internal.io.Schema;
import com.continuuity.internal.io.TypeRepresentation;

import java.lang.reflect.Type;
import javax.annotation.Nullable;

/**
 *
 */
public class PurchaseHistoryStore extends ObjectStoreDataset<PurchaseHistory>
  implements RecordScannable<KeyValue<byte[], PurchaseHistory>> {

  public PurchaseHistoryStore(String name, KeyValueTable kvTable, TypeRepresentation typeRep, Schema schema,
                              @Nullable ClassLoader classLoader) {
    super(name, kvTable, typeRep, schema, classLoader);
  }

  @Override
  public Type getRecordType() {
    return PurchaseHistory.class;
  }

  @Override
  public RecordScanner<KeyValue<byte[], PurchaseHistory>> createSplitRecordScanner(Split split) {
    return Scannables.splitRecordScanner(createSplitReader(split), new PurchaseHistoryRecordMaker());
  }

  /**
   * {@link com.continuuity.api.data.batch.Scannables.RecordMaker} for {@link PurchaseHistoryStore}.
   */
  public class PurchaseHistoryRecordMaker
    implements Scannables.RecordMaker<byte[], PurchaseHistory, KeyValue<byte[], PurchaseHistory>> {

    @Override
    public KeyValue<byte[], PurchaseHistory> makeRecord(byte[] key, PurchaseHistory value) {
      return new KeyValue<byte[], PurchaseHistory>(key, value);
    }
  }

}
