package com.continuuity.examples.dataset;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.lib.CompositeDatasetAdmin;
import com.continuuity.api.dataset.table.Table;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class SampleDatasetAdmin extends CompositeDatasetAdmin {

  private final Map<ByteArrayWrapper, DatasetAdmin> indexAdmins;
  private final DatasetAdmin tableAdmin;
  private final DatasetAdmin metaAdmin;

  public SampleDatasetAdmin(DatasetAdmin tableAdmin, DatasetAdmin metaAdmin,
                            Map<ByteArrayWrapper, DatasetAdmin> indexAdmins) {

    super(ImmutableList.<DatasetAdmin>builder().add(tableAdmin).add(metaAdmin).addAll(indexAdmins.values()).build());
    this.tableAdmin = tableAdmin;
    this.metaAdmin = metaAdmin;
    this.indexAdmins = indexAdmins;
  }

  // TODO: may want to drop instead of just truncate
  public void deleteIndex(byte[] indexedColumn) throws IOException {
    DatasetAdmin indexTableAdmin = getIndexTableAdmin(indexedColumn);
    indexTableAdmin.truncate();
  }

  // TODO: duplicates code in SampleDataset
  private DatasetAdmin getIndexTableAdmin(byte[] column) {
    byte[] key = Bytes.toBytes("idx_" + Bytes.toString(column));
    return indexAdmins.get(new ByteArrayWrapper(key));
  }

  private boolean isIgnoredIndex(byte[] column) {
    byte[] key = Bytes.toBytes("idx_" + Bytes.toString(column));
    return !indexAdmins.containsKey(new ByteArrayWrapper(key));
  }
}
