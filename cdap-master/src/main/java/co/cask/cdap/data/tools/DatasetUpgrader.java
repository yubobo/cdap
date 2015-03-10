/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.data.tools;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.dataset.DatasetAdmin;
import co.cask.cdap.api.dataset.DatasetDefinition;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.io.Locations;
import co.cask.cdap.data2.datafabric.dataset.DatasetMetaTableUtil;
import co.cask.cdap.data2.datafabric.dataset.DatasetsUtil;
import co.cask.cdap.data2.datafabric.dataset.service.mds.DatasetInstanceMDS;
import co.cask.cdap.data2.datafabric.dataset.service.mds.DatasetTypeMDS;
import co.cask.cdap.data2.datafabric.dataset.type.DatasetTypeManager;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.dataset2.DatasetManagementException;
import co.cask.cdap.data2.dataset2.lib.hbase.AbstractHBaseDataSetAdmin;
import co.cask.cdap.data2.dataset2.lib.table.MDSKey;
import co.cask.cdap.data2.dataset2.lib.table.hbase.HBaseTableAdmin;
import co.cask.cdap.data2.dataset2.tx.Transactional;
import co.cask.cdap.data2.transaction.queue.QueueAdmin;
import co.cask.cdap.data2.util.TableId;
import co.cask.cdap.data2.util.hbase.HBaseTableUtil;
import co.cask.cdap.data2.util.hbase.HTableNameConverter;
import co.cask.cdap.data2.util.hbase.HTableNameConverterFactory;
import co.cask.cdap.proto.DatasetModuleMeta;
import co.cask.cdap.proto.DatasetSpecificationSummary;
import co.cask.cdap.proto.Id;
import co.cask.tephra.TransactionExecutor;
import co.cask.tephra.TransactionExecutorFactory;
import co.cask.tephra.TransactionFailureException;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Handles upgrade for System and User Datasets
 */
public class DatasetUpgrader extends AbstractUpgrader {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUpgrader.class);

  private final CConfiguration cConf;
  private final Configuration hConf;
  private final LocationFactory locationFactory;
  private final QueueAdmin queueAdmin;
  private final HBaseTableUtil hBaseTableUtil;
  private final DatasetFramework dsFramework;
  private static final Pattern USER_TABLE_PREFIX = Pattern.compile("^cdap\\.user\\..*");
  // lists of datasets type modules which existed earlier but does not anymore
  private static final Set<String> REMOVED_DATASET_MODULES = Sets.newHashSet(
    "co.cask.cdap.data2.dataset2.module.lib.hbase.HBaseOrderedTableModule",
    "co.cask.cdap.data2.dataset2.lib.table.ACLTableModule");

  private final Transactional<UpgradeMdsStores<DatasetTypeMDS>, DatasetTypeMDS> datasetTypeMDS;
  private final Transactional<UpgradeMdsStores<DatasetInstanceMDS>, DatasetInstanceMDS> datasetInstanceMds;

  @Inject
  private DatasetUpgrader(CConfiguration cConf, Configuration hConf, LocationFactory locationFactory,
                          QueueAdmin queueAdmin, HBaseTableUtil hBaseTableUtil,
                          final TransactionExecutorFactory executorFactory,
                          @Named("dsFramework") final DatasetFramework dsFramework) {

    super(locationFactory);
    this.cConf = cConf;
    this.hConf = hConf;
    this.locationFactory = locationFactory;
    this.queueAdmin = queueAdmin;
    this.hBaseTableUtil = hBaseTableUtil;
    this.dsFramework = dsFramework;

    this.datasetTypeMDS = Transactional.of(executorFactory, new Supplier<UpgradeMdsStores<DatasetTypeMDS>>() {
      @Override
      public UpgradeMdsStores<DatasetTypeMDS> get() {
        String dsName = Joiner.on(".").join(Constants.SYSTEM_NAMESPACE, DatasetMetaTableUtil.META_TABLE_NAME);
        Id.DatasetInstance datasetId = Id.DatasetInstance.from(Constants.DEFAULT_NAMESPACE_ID, dsName);
        try {
          DatasetTypeMDS oldMds =
            DatasetsUtil.getOrCreateDataset(dsFramework, datasetId, DatasetTypeMDS.class.getName(),
                                            DatasetProperties.EMPTY, DatasetDefinition.NO_ARGUMENTS, null);
          DatasetTypeMDS newMds = new DatasetMetaTableUtil(dsFramework).getTypeMetaTable();
          return new UpgradeMdsStores<DatasetTypeMDS>(oldMds, newMds);
        } catch (Exception e) {
          LOG.error("Failed to access table: {}", datasetId, e);
          throw Throwables.propagate(e);
        }
      }
    });

    this.datasetInstanceMds = Transactional.of(executorFactory, new Supplier<UpgradeMdsStores<DatasetInstanceMDS>>() {
      @Override
      public UpgradeMdsStores<DatasetInstanceMDS> get() {
        String dsName = Joiner.on(".").join(Constants.SYSTEM_NAMESPACE, DatasetMetaTableUtil.INSTANCE_TABLE_NAME);
        Id.DatasetInstance datasetId = Id.DatasetInstance.from(Constants.DEFAULT_NAMESPACE_ID, dsName);
        try {
          DatasetInstanceMDS oldMds = DatasetsUtil.getOrCreateDataset(dsFramework, datasetId,
                                                                      DatasetInstanceMDS.class.getName(),
                                                                      DatasetProperties.EMPTY,
                                                                      DatasetDefinition.NO_ARGUMENTS, null);
          DatasetInstanceMDS newMds = new DatasetMetaTableUtil(dsFramework).getInstanceMetaTable();
          return new UpgradeMdsStores<DatasetInstanceMDS>(oldMds, newMds);
        } catch (Exception e) {
          LOG.error("Failed to access table: {}", datasetId, e);
          throw Throwables.propagate(e);
        }
      }
    });
  }

  @Override
  public void upgrade() throws Exception {
    // Upgrade system dataset
    upgradeSystemDatasets();

    // Upgrade all user hbase tables
    upgradeUserTables();

    // Upgrade all queue and stream tables.
    queueAdmin.upgrade();

    // Upgrade the datasets type meta table
    upgradeDatasetTypeMDS();

    // Upgrade the datasets instance meta table
    upgradeDatasetInstanceMDS();
  }

  private void upgradeSystemDatasets() throws Exception {

    // Upgrade all datasets in system namespace
    for (DatasetSpecificationSummary spec : dsFramework.getInstances(Constants.DEFAULT_NAMESPACE_ID)) {
      LOG.info("Upgrading dataset: {}, spec: {}", spec.getName(), spec.toString());
      DatasetAdmin admin = dsFramework.getAdmin(Id.DatasetInstance.from(Constants.DEFAULT_NAMESPACE_ID, spec.getName()),
                                                null);
      // we know admin is not null, since we are looping over existing datasets
      admin.upgrade();
      LOG.info("Upgraded dataset: {}", spec.getName());
    }
  }

  private void upgradeUserTables() throws Exception {
    HBaseAdmin hAdmin = new HBaseAdmin(hConf);

    for (HTableDescriptor desc : hAdmin.listTables(USER_TABLE_PREFIX)) {
      String tableName = desc.getNameAsString();
      HTableNameConverter hTableNameConverter = new HTableNameConverterFactory().get();
      TableId tableId = hTableNameConverter.from(tableName);
      LOG.info("Upgrading hbase table: {}, desc: {}", tableName, desc);

      final boolean supportsIncrement = HBaseTableAdmin.supportsReadlessIncrements(desc);
      final boolean transactional = HBaseTableAdmin.isTransactional(desc);
      DatasetAdmin admin = new AbstractHBaseDataSetAdmin(tableId, hConf, hBaseTableUtil) {
        @Override
        protected CoprocessorJar createCoprocessorJar() throws IOException {
          return HBaseTableAdmin.createCoprocessorJarInternal(cConf,
                                                              locationFactory,
                                                              hBaseTableUtil,
                                                              transactional,
                                                              supportsIncrement);
        }

        @Override
        protected boolean upgradeTable(HTableDescriptor tableDescriptor) {
          // we don't do any other changes apart from coprocessors upgrade
          return false;
        }

        @Override
        public void create() throws IOException {
          // no-op
          throw new UnsupportedOperationException("This DatasetAdmin is only used for upgrade() operation");
        }
      };
      admin.upgrade();
      LOG.info("Upgraded hbase table: {}", tableName);
    }
  }


  /**
   * Upgrades the {@link DatasetTypeMDS} table for namespaces
   * Note: We don't write to new TypeMDS table through {@link DatasetTypeManager} because if the user's custom Datasets
   * has api/classes changes {@link DatasetTypeManager#addModule} will fail. So, we directly move the meta type
   * information. User's custom datasets which don't use any such changed api/classes will work out of the box but
   * the one which does will need to be re-deployed.
   *
   * @throws TransactionFailureException
   * @throws InterruptedException
   * @throws IOException
   */
  private void upgradeDatasetTypeMDS() throws Exception {
    final MDSKey dsModulePrefix = new MDSKey(Bytes.toBytes(DatasetTypeMDS.MODULES_PREFIX));
    try {
      datasetTypeMDS.execute(new TransactionExecutor.Function<UpgradeMdsStores<DatasetTypeMDS>, Void>() {
        @Override
        public Void apply(UpgradeMdsStores<DatasetTypeMDS> ctx) throws Exception {
          Map<MDSKey, DatasetModuleMeta> mdsKeyDatasetModuleMetaMap = ctx.getOldMds().listKV(dsModulePrefix,
                                                                                             DatasetModuleMeta.class);
          for (DatasetModuleMeta datasetModuleMeta : mdsKeyDatasetModuleMetaMap.values()) {
            if (!REMOVED_DATASET_MODULES.contains(datasetModuleMeta.getClassName())) {
              upgradeDatasetModuleMeta(datasetModuleMeta, ctx.getNewMds());
            }
          }
          return null;
        }
      });
    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Upgrades the {@link DatasetModuleMeta} for namespace
   * The system modules are written as it is and the user module meta is written with new jarLocation which is
   * under namespace
   *
   * @param olddatasetModuleMeta the old {@link DatasetModuleMeta}
   * @param datasetTypeMDS the new {@link DatasetTypeMDS} where the new moduleMeta will be written
   * @throws IOException
   */
  private void upgradeDatasetModuleMeta(DatasetModuleMeta olddatasetModuleMeta, DatasetTypeMDS datasetTypeMDS)
    throws IOException {
    DatasetModuleMeta newDatasetModuleMeta;
    LOG.info("Upgrading dataset module {} meta", olddatasetModuleMeta.getName());
    if (olddatasetModuleMeta.getJarLocation() == null) {
      newDatasetModuleMeta = olddatasetModuleMeta;
    } else {
      Location oldJarLocation = locationFactory.create(olddatasetModuleMeta.getJarLocation());
      Location newJarLocation = updateUserDatasetModuleJarLocation(oldJarLocation, olddatasetModuleMeta.getClassName(),
                                                                   Constants.DEFAULT_NAMESPACE);

      newDatasetModuleMeta = new DatasetModuleMeta(olddatasetModuleMeta.getName(), olddatasetModuleMeta.getClassName(),
                                                   newJarLocation.toURI(), olddatasetModuleMeta.getTypes(),
                                                   olddatasetModuleMeta.getUsesModules());
      // add usedByModules to the newdatasetModuleMeta
      Collection<String> usedByModules = olddatasetModuleMeta.getUsedByModules();
      for (String moduleName : usedByModules) {
        newDatasetModuleMeta.addUsedByModule(moduleName);
      }
      newDatasetModuleMeta = olddatasetModuleMeta;
      renameLocation(oldJarLocation, newJarLocation);
    }
    datasetTypeMDS.writeModule(Constants.DEFAULT_NAMESPACE_ID, newDatasetModuleMeta);
  }

  /**
   * Strips different parts from the old jar location and creates a new one
   *
   * @param location the old log {@link Location}
   * @param datasetClassname the dataset class name
   * @param namespace the namespace which will be added to the new jar location
   * @return the log {@link Location}
   * @throws IOException
   */
  private Location updateUserDatasetModuleJarLocation(Location location, String datasetClassname,
                                                      String namespace) throws IOException {
    String jarFilename = location.getName();
    Location parentLocation = Locations.getParent(location);  // strip jarFilename
    parentLocation = Locations.getParent(parentLocation); // strip account_placeholder
    String archive = parentLocation != null ? parentLocation.getName() : null;
    parentLocation = Locations.getParent(parentLocation); // strip archive
    String datasets = parentLocation != null ? parentLocation.getName() : null;

    return locationFactory.create(namespace).append(datasets).append(datasetClassname).append(archive)
      .append(jarFilename);
  }

  // Moves dataset instance meta entries into new table (in system namespace)
  // Also updates the spec's name ('cdap.user.foo' -> 'foo')
  private void upgradeDatasetInstanceMDS() throws IOException, DatasetManagementException, InterruptedException,
    TransactionFailureException {
    LOG.info("Upgrading dataset instance mds.");
    datasetInstanceMds.execute(new TransactionExecutor.Function<UpgradeMdsStores<DatasetInstanceMDS>, Void>() {
      @Override
      public Void apply(UpgradeMdsStores<DatasetInstanceMDS> ctx) throws Exception {
        MDSKey key = new MDSKey(Bytes.toBytes(DatasetInstanceMDS.INSTANCE_PREFIX));
        DatasetInstanceMDS newMds = ctx.getNewMds();
        Map<MDSKey, DatasetSpecification> dsSpecs = ctx.getOldMds().listKV(key, DatasetSpecification.class);
        for (Map.Entry<MDSKey, DatasetSpecification> dsSpecEntry : dsSpecs.entrySet()) {
          DatasetSpecification dsSpec = dsSpecEntry.getValue();
          LOG.info("Migrating dataset Spec: {}", dsSpec);
          Id.Namespace namespace = namespaceFromDatasetName(dsSpec.getName());
          DatasetSpecification migratedDsSpec = migrateDatasetSpec(dsSpec);
          LOG.info("Writing new dataset Spec: {}", migratedDsSpec);
          newMds.write(namespace, migratedDsSpec);
        }
        return null;
      }
    });
  }

  /**
   * Construct a {@link TableId} from a dataset name
   * TODO: CDAP-1593 - This is bad and should be removed, since it makes assumptions about the dataset name format.
   *
   * @param name the dataset/table name to construct the {@link TableId} from
   * @return the {@link TableId} object for the specified dataset/table name
   */
  private static TableId from(String name) {
    Preconditions.checkArgument(name != null, "Dataset name should not be null");
    // Dataset/Table name is expected to be in the format <table-prefix>.<namespace>.<name>
    String invalidFormatError = String.format("Invalid format for dataset/table name '%s'. " +
                                                "Expected - <table-prefix>.<namespace>.<dataset/table-name>", name);
    String[] parts = name.split("\\.", 3);
    Preconditions.checkArgument(parts.length == 3, invalidFormatError);
    // Ignore the prefix in the input name.
    return TableId.from(Id.Namespace.from(parts[1]), parts[2]);
  }

  private DatasetSpecification migrateDatasetSpec(DatasetSpecification oldSpec) {
    TableId from = from(oldSpec.getName());
    String newDatasetName = from.getTableName();
    return DatasetSpecification.changeName(oldSpec, newDatasetName);
  }

  private Id.Namespace namespaceFromDatasetName(String dsName) {
    // input of the form: 'cdap.user.foo', or 'cdap.system.app.meta'
    TableId tableId = from(dsName);
    String namespace = tableId.getNamespace().getId();
    if (Constants.SYSTEM_NAMESPACE.equals(namespace)) {
      return Constants.SYSTEM_NAMESPACE_ID;
    } else if ("user".equals(namespace)) {
      return Constants.DEFAULT_NAMESPACE_ID;
    } else {
      throw new IllegalArgumentException(String.format("Expected dataset namespace to be either 'system' or 'user': %s",
                                                       tableId));
    }
  }

  private static final class UpgradeMdsStores<T> implements Iterable<T> {
    private final List<T> stores;

    private UpgradeMdsStores(T oldMds, T newMds) {
      this.stores = ImmutableList.of(oldMds, newMds);
    }

    private T getOldMds() {
      return stores.get(0);
    }

    private T getNewMds() {
      return stores.get(1);
    }

    @Override
    public Iterator<T> iterator() {
      return stores.iterator();
    }
  }
}
