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
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.io.Locations;
import co.cask.cdap.data2.datafabric.dataset.DatasetMetaTableUtil;
import co.cask.cdap.data2.datafabric.dataset.DatasetsUtil;
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
import co.cask.cdap.internal.app.store.DefaultStore;
import co.cask.cdap.proto.DatasetModuleMeta;
import co.cask.cdap.proto.DatasetSpecificationSummary;
import co.cask.cdap.proto.Id;
import co.cask.tephra.TransactionExecutor;
import co.cask.tephra.TransactionExecutorFactory;
import co.cask.tephra.TransactionFailureException;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
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
import java.util.Iterator;
import java.util.Map;
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
  private final DatasetTypeManager datasetTypeManager;
  private static final Pattern USER_TABLE_PREFIX = Pattern.compile("^cdap\\.user\\..*");
  private final Transactional<AppMDS, DatasetTypeMDS> oldDatasetTypeMDS;
//  private final DatasetTypeMDS oldDatasetTypeMDS;

  @Inject
  private DatasetUpgrader(CConfiguration cConf, Configuration hConf, LocationFactory locationFactory,
                          QueueAdmin queueAdmin, HBaseTableUtil hBaseTableUtil,
                          final TransactionExecutorFactory executorFactory,
                          @Named("dsFramework") final DatasetFramework dsFramework,
                          @Named("datasetTypeManager") DatasetTypeManager datasetTypeManager)
    throws IOException, DatasetManagementException {

    super(locationFactory);
    this.cConf = cConf;
    this.hConf = hConf;
    this.locationFactory = locationFactory;
    this.queueAdmin = queueAdmin;
    this.hBaseTableUtil = hBaseTableUtil;
    this.dsFramework = dsFramework;
    this.datasetTypeManager = datasetTypeManager;
    this.oldDatasetTypeMDS = Transactional.of(executorFactory, new Supplier<AppMDS>() {
      @Override
      public AppMDS get() {
        try {
          DatasetTypeMDS table = DatasetsUtil.getOrCreateDataset(
            dsFramework, Id.DatasetInstance.from(Constants.DEFAULT_NAMESPACE_ID,
                                                 Joiner.on(".").join(Constants.SYSTEM_NAMESPACE,
                                                                     DatasetMetaTableUtil.META_TABLE_NAME)),
            DatasetTypeMDS.class.getName(), DatasetProperties.EMPTY, DatasetDefinition.NO_ARGUMENTS, null);
          return new AppMDS(table);
        } catch (Exception e) {
          LOG.error("Failed to access {} table", Joiner.on(".").join(Constants.SYSTEM_NAMESPACE,
                                                                     DefaultStore.APP_META_TABLE), e);
          throw Throwables.propagate(e);
        }
      }
    });
  }

  @Override
  public void upgrade() throws Exception {
    // Upgrade system dataset
    upgradeSystemDatasets(dsFramework);

    // Upgrade all user hbase tables
    upgradeUserTables();

    // Upgrade all queue and stream tables.
    queueAdmin.upgrade();

    // upgrade dataset type meta
    upgradeDatasetTypeMDS();
  }

  private void upgradeSystemDatasets(DatasetFramework framework) throws Exception {

    // Upgrade all datasets in system namespace
    for (DatasetSpecificationSummary spec : framework.getInstances(Constants.DEFAULT_NAMESPACE_ID)) {
      LOG.info("Upgrading dataset: {}, spec: {}", spec.getName(), spec.toString());
      DatasetAdmin admin = framework.getAdmin(Id.DatasetInstance.from(Constants.DEFAULT_NAMESPACE_ID, spec.getName()),
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

  private void upgradeDatasetTypeMDS() throws TransactionFailureException,
    InterruptedException, IOException {
    LOG.info("YOO! In function");
    final MDSKey dsModulePrefix = new MDSKey(Bytes.toBytes(DatasetTypeMDS.MODULES_PREFIX));


    try {
      oldDatasetTypeMDS.execute(new TransactionExecutor.Function<AppMDS, Void>() {
        @Override
        public Void apply(AppMDS ctx) throws Exception {
          Map<MDSKey, DatasetModuleMeta> mdsKeyDatasetModuleMetaMap = ctx.oldMDS.listKV(dsModulePrefix,
                                                                                     DatasetModuleMeta.class);
          for (DatasetModuleMeta datasetModuleMeta : mdsKeyDatasetModuleMetaMap.values()) {
            if (!(datasetModuleMeta.getClassName().equals(
              "co.cask.cdap.data2.dataset2.module.lib.hbase.HBaseOrderedTableModule") ||
              datasetModuleMeta.getClassName().equals("co.cask.cdap.data2.dataset2.lib.table.ACLTableModule"))) {
              datasetModuleUpgrader(datasetModuleMeta);
            }
          }
          return null;
        }
      });
    } catch (Exception e) {
      Throwables.propagate(e);
    }
  }

  private void datasetModuleUpgrader(DatasetModuleMeta datasetModuleMeta) throws IOException {
    DatasetModuleMeta newDatasetModuleMeta;
    if (datasetModuleMeta.getJarLocation() == null) {
      LOG.info("YOO! Inside system");
      LOG.info("Writing new module meta {}", datasetModuleMeta);
      try {
        datasetTypeManager.addModule(Id.DatasetModule.from(Constants.SYSTEM_NAMESPACE_ID, datasetModuleMeta.getName()),
                                     datasetModuleMeta.getClassName(),
                                     null);
      } catch (Throwable t) {
        LOG.error("Failed to write system module meta to new table", t);
      }
    } else {
      LOG.info("YOO! Inside user");
      Location oldJarLocation = locationFactory.create(datasetModuleMeta.getJarLocation());
      Location newJarLocation = updateUserDatasetModuleJarLocation(oldJarLocation, datasetModuleMeta.getClassName(),
                                                                   Constants.DEFAULT_NAMESPACE);

      newDatasetModuleMeta = new DatasetModuleMeta(datasetModuleMeta.getName(), datasetModuleMeta.getClassName(),
                                                   newJarLocation.toURI(), datasetModuleMeta.getTypes(),
                                                   datasetModuleMeta.getUsesModules());
      LOG.info("Writing new module meta {}", newDatasetModuleMeta);
      renameLocation(oldJarLocation, newJarLocation);
      try {
        datasetTypeManager.addModule(Id.DatasetModule.from(Constants.DEFAULT_NAMESPACE_ID,
                                                           newDatasetModuleMeta.getName()),
                                     newDatasetModuleMeta.getClassName(),
                                     locationFactory.create(newDatasetModuleMeta.getJarLocation()));
      } catch (Throwable t) {
        LOG.error("Failed to write user module meta to new table", t);
      }
    }
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

  private static final class AppMDS implements Iterable<DatasetTypeMDS> {
    private final DatasetTypeMDS oldMDS;

    private AppMDS(DatasetTypeMDS metaTable) {
      this.oldMDS = metaTable;
    }

    @Override
    public Iterator<DatasetTypeMDS> iterator() {
      return Iterators.singletonIterator(oldMDS);
    }
  }
}
