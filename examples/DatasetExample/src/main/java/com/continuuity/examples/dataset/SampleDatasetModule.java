package com.continuuity.examples.dataset;

import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetDefinition;
import com.continuuity.api.dataset.module.DatasetDefinitionRegistry;
import com.continuuity.api.dataset.module.DatasetModule;
import com.continuuity.api.dataset.table.Table;

/**
 * Dataset module for {@link SampleDataset}.
 */
public class SampleDatasetModule implements DatasetModule {

  @Override
  public void register(DatasetDefinitionRegistry registry) {
    DatasetDefinition<Table, DatasetAdmin> tableDef = registry.get(Table.class.getName());
    registry.add(new SampleDatasetDefinition("sample", tableDef));
    registry.add(new SampleDatasetDefinition(SampleDataset.class.getName(), tableDef));
  }
}
