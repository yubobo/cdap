package com.continuuity.data2.dataset2.lib.table;

import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetDefinition;
import com.continuuity.api.dataset.DatasetProperties;
import com.continuuity.api.dataset.DatasetSpecification;
import com.continuuity.api.dataset.table.Table;
import com.continuuity.data2.dataset2.lib.AbstractDatasetDefinition;
import com.continuuity.data2.dataset2.lib.CompositeDatasetAdmin;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * {@link com.continuuity.api.dataset.DatasetDefinition} for {@link com.continuuity.data2.dataset2.lib.table.KeyValueTable}.
 */
public class UniqueCountTableDefinition
  extends AbstractDatasetDefinition<UniqueCountTable, DatasetAdmin> {

  private final DatasetDefinition<? extends Table, ?> tableDef;

  public UniqueCountTableDefinition(String name, DatasetDefinition<? extends Table, ?> tableDef) {
    super(name);
    Preconditions.checkArgument(tableDef != null, "Table definition is required");
    this.tableDef = tableDef;
  }

  @Override
  public DatasetSpecification configure(String instanceName, DatasetProperties properties) {
    return DatasetSpecification.builder(instanceName, getName())
      .properties(properties.getProperties())
      .datasets(tableDef.configure("entryCountTable", properties))
      .datasets(tableDef.configure("uniqueCountTable", properties))
      .build();
  }

  @Override
  public DatasetAdmin getAdmin(DatasetSpecification spec) throws IOException {
    return new CompositeDatasetAdmin(tableDef.getAdmin(spec.getSpecification("entryCountTable")),
                                     tableDef.getAdmin(spec.getSpecification("uniqueCountTable")));
  }

  @Override
  public UniqueCountTable getDataset(DatasetSpecification spec) throws IOException {
    return new UniqueCountTable(spec.getName(),
                                tableDef.getDataset(spec.getSpecification("entryCountTable")),
                                tableDef.getDataset(spec.getSpecification("uniqueCountTable")));
  }
}
