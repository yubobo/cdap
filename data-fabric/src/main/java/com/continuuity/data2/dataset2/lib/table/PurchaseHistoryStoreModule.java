package com.continuuity.data2.dataset2.lib.table;

import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetDefinition;
import com.continuuity.api.dataset.lib.KeyValueTable;
import com.continuuity.api.dataset.module.DatasetDefinitionRegistry;
import com.continuuity.api.dataset.module.DatasetModule;

/**
 * Depends on {@link KeyValueTable}.
 */
public class PurchaseHistoryStoreModule implements DatasetModule {

  @Override
  public void register(DatasetDefinitionRegistry registry) {
    DatasetDefinition<KeyValueTable, DatasetAdmin> keyValueTableDef = registry.get("keyValueTable");

    DatasetDefinition<PurchaseHistoryStore, DatasetAdmin> purchaseStoreDef =
      new PurchaseHistoryStoreDefinition("purchaseHistoryStore", keyValueTableDef);
    registry.add(purchaseStoreDef);
    registry.add(new PurchaseHistoryStoreDefinition(PurchaseHistoryStore.class.getName(), keyValueTableDef));
  }

}
