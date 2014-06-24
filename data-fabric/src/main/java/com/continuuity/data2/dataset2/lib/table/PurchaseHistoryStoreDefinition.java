package com.continuuity.data2.dataset2.lib.table;

import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetDefinition;
import com.continuuity.api.dataset.DatasetProperties;
import com.continuuity.api.dataset.DatasetSpecification;
import com.continuuity.api.dataset.lib.AbstractDatasetDefinition;
import com.continuuity.api.dataset.lib.KeyValueTable;
import com.continuuity.api.dataset.lib.ObjectStore;
import com.continuuity.internal.io.ReflectionSchemaGenerator;
import com.continuuity.internal.io.Schema;
import com.continuuity.internal.io.SchemaTypeAdapter;
import com.continuuity.internal.io.TypeRepresentation;
import com.continuuity.internal.io.UnsupportedTypeException;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * DatasetDefinition for {@link PurchaseHistoryStore}.
 */
public class PurchaseHistoryStoreDefinition
  extends AbstractDatasetDefinition<PurchaseHistoryStore, DatasetAdmin> {

  private final DatasetDefinition<? extends KeyValueTable, ?> tableDef;

  public PurchaseHistoryStoreDefinition(String name, DatasetDefinition<? extends KeyValueTable, ?> keyValueDef) {
    super(name);
    Preconditions.checkArgument(keyValueDef != null, "KeyValueTable definition is required");
    this.tableDef = keyValueDef;
  }

  @Override
  public DatasetSpecification configure(String instanceName, DatasetProperties properties) {
    return DatasetSpecification.builder(instanceName, getName())
      .properties(properties.getProperties())
      .datasets(tableDef.configure("objects", properties))
      .build();
  }

  @Override
  public DatasetAdmin getAdmin(DatasetSpecification spec, ClassLoader classLoader) throws IOException {
    return tableDef.getAdmin(spec.getSpecification("objects"), classLoader);
  }

  @Override
  public PurchaseHistoryStore getDataset(DatasetSpecification spec, ClassLoader classLoader) throws IOException {
    DatasetSpecification kvTableSpec = spec.getSpecification("objects");
    KeyValueTable table = tableDef.getDataset(kvTableSpec, classLoader);

    try {
      Type type = PurchaseHistory.class;
      Schema schema = new ReflectionSchemaGenerator().generate(type);
      TypeRepresentation typeRep = new TypeRepresentation(type);
      return new PurchaseHistoryStore(spec.getName(), table, typeRep, schema, classLoader);
    } catch (UnsupportedTypeException e) {
      throw new IOException(e);
    }
  }

}
