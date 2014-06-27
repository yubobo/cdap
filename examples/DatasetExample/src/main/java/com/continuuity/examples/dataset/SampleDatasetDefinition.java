package com.continuuity.examples.dataset;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.dataset.Dataset;
import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetDefinition;
import com.continuuity.api.dataset.DatasetProperties;
import com.continuuity.api.dataset.DatasetSpecification;
import com.continuuity.api.dataset.lib.AbstractDatasetDefinition;
import com.continuuity.api.dataset.lib.CompositeDatasetAdmin;
import com.continuuity.api.dataset.lib.KeyValueTable;
import com.continuuity.api.dataset.table.Table;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Dataset definition for {@link SampleDataset}.
 */
public class SampleDatasetDefinition extends AbstractDatasetDefinition<SampleDataset, SampleDatasetAdmin> {

  private static final Gson GSON = new Gson();

  private final DatasetDefinition<? extends Table, ?> tableDef;

  public SampleDatasetDefinition(String name, DatasetDefinition<? extends Table, ?> tableDef) {
    super(name);
    this.tableDef = tableDef;
  }

  public static DatasetProperties properties(String timestampField, String[] indices) {
    return DatasetProperties.builder()
      .add("indices", GSON.toJson(indices))
      .add("timestampField", timestampField)
      .build();
  }

  @Override
  public DatasetSpecification configure(String instanceName, DatasetProperties properties) {
    List<DatasetSpecification> datasets = Lists.newArrayList();
    datasets.add(tableDef.configure("data", properties));
    datasets.add(tableDef.configure("meta", properties));

    for (String index : getIndexNames(properties.getProperties().get("indices"))) {
      datasets.add(tableDef.configure(index, properties));
    }

    return DatasetSpecification.builder(instanceName, getName())
      .properties(properties.getProperties())
      .datasets(datasets)
      .build();
  }

  @Override
  public SampleDatasetAdmin getAdmin(DatasetSpecification spec, ClassLoader classLoader) throws IOException {
    DatasetAdmin tableAdmin = tableDef.getAdmin(spec.getSpecification("data"), classLoader);
    DatasetAdmin metaAdmin = tableDef.getAdmin(spec.getSpecification("meta"), classLoader);

    Map<ByteArrayWrapper, DatasetAdmin> indexAdmins = Maps.newHashMap();
    for (String index : getIndexNames(spec.getProperty("indices"))) {
      indexAdmins.put(new ByteArrayWrapper(Bytes.toBytes(index)),
                      tableDef.getAdmin(spec.getSpecification(index), classLoader));
    }

    return new SampleDatasetAdmin(tableAdmin, metaAdmin, indexAdmins);
  }

  @Override
  public SampleDataset getDataset(DatasetSpecification spec, ClassLoader classLoader) throws IOException {
    DatasetSpecification tableSpec = spec.getSpecification("data");
    Table data = tableDef.getDataset(tableSpec, classLoader);

    DatasetSpecification metaSpec = spec.getSpecification("meta");
    Table meta = tableDef.getDataset(tableSpec, classLoader);

    Map<ByteArrayWrapper, Table> indices = Maps.newHashMap();
    for (String index : getIndexNames(spec.getProperty("indices"))) {
      indices.put(new ByteArrayWrapper(Bytes.toBytes(index)),
                  tableDef.getDataset(spec.getSpecification(index), classLoader));
    }

    return new SampleDataset(spec, data, meta, indices);
  }

  private List<String> getIndexNames(String indicesProperty) {
    String[] indices = GSON.fromJson(indicesProperty, String[].class);

    List<String> indexNames = Lists.newArrayList();
    for (String index : indices) {
      indexNames.add("idx_" + index);
    }
    return indexNames;
  }
}
