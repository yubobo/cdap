package com.continuuity.data2.dataset2.lib.table;

import com.continuuity.api.dataset.lib.AbstractDataset;
import com.continuuity.api.dataset.table.Get;
import com.continuuity.api.dataset.table.Increment;
import com.continuuity.api.dataset.table.Table;

/**
 *
 */
public class UniqueCountTable2 extends AbstractDataset {

  private final Table entryCountTable;
  private final Table uniqueCountTable;

  public UniqueCountTable2(String instanceName,
                           Table entryCountTable,
                           Table uniqueCountTable) {
    super(instanceName, entryCountTable, uniqueCountTable);
    this.entryCountTable = entryCountTable;
    this.uniqueCountTable = uniqueCountTable;
  }

  public void updateUniqueCount(String entry) {
    long newCount = entryCountTable.increment(new Increment(entry, "count", 1L)).getInt("count");
    if (newCount == 1L) {
      uniqueCountTable.increment(new Increment("unique_count", "count", 1L));
    }
  }


  public Long readUniqueCount() {
    return uniqueCountTable.get(new Get("unique_count", "count")).getLong("count");
  }

}
