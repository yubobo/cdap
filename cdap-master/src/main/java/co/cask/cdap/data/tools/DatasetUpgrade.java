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
import co.cask.cdap.api.dataset.table.Row;
import co.cask.cdap.api.dataset.table.Scanner;
import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.data2.datafabric.DefaultDatasetNamespace;
import co.cask.cdap.data2.datafabric.dataset.DatasetMetaTableUtil;
import co.cask.cdap.data2.datafabric.dataset.DatasetsUtil;
import co.cask.cdap.data2.datafabric.dataset.service.mds.DatasetTypeMDS;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.dataset2.DatasetManagementException;
import co.cask.cdap.data2.dataset2.lib.hbase.AbstractHBaseDataSetAdmin;
import co.cask.cdap.data2.dataset2.lib.table.hbase.HBaseTableAdmin;
import co.cask.cdap.data2.dataset2.tx.DatasetContext;
import co.cask.cdap.data2.dataset2.tx.Transactional;
import co.cask.cdap.data2.transaction.queue.QueueAdmin;
import co.cask.cdap.data2.util.hbase.HBaseTableUtil;
import co.cask.cdap.data2.util.hbase.TableId;
import co.cask.cdap.internal.app.store.DefaultStore;
import co.cask.cdap.proto.DatasetModuleMeta;
import co.cask.cdap.proto.Id;
import co.cask.tephra.DefaultTransactionExecutor;
import co.cask.tephra.TransactionAware;
import co.cask.tephra.TransactionExecutor;
import co.cask.tephra.TransactionExecutorFactory;
import co.cask.tephra.TransactionFailureException;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.twill.filesystem.LocationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Handles upgrade for System and User Datasets
 */
public class DatasetUpgrade extends AbstractUpgrade implements Upgrade {

  private static final Logger LOG = LoggerFactory.getLogger(MDSUpgrade.class);

  private final Transactional<DatasetContext<Table>, Table> datasetTypeMDS;

  public DatasetUpgrade() {
    this.datasetTypeMDS = Transactional.of(
      new TransactionExecutorFactory() {
        @Override
        public TransactionExecutor createExecutor(Iterable<TransactionAware> txAwares) {
          return new DefaultTransactionExecutor(txClient, txAwares);
        }
      },
      new Supplier<DatasetContext<Table>>() {
        @Override
        public DatasetContext<Table> get() {
          try {
            Table table = DatasetsUtil.getOrCreateDataset(namespacedFramework, Id.DatasetInstance.from
                                                            (Constants.SYSTEM_NAMESPACE_ID, 
                                                             DatasetMetaTableUtil.META_TABLE_NAME), "table",
                                                          DatasetProperties.EMPTY, 
                                                          DatasetDefinition.NO_ARGUMENTS, null);
            return DatasetContext.of(table);
          } catch (Exception e) {
            LOG.error("Failed to access {} table", DefaultStore.APP_META_TABLE, e);
            throw Throwables.propagate(e);
          }
        }
      });
  }

  @Override
  public void upgrade(Injector injector) throws Exception {
    // Upgrade system dataset
    upgradeSystemDatasets(injector, namespacedFramework);

    // Upgrade all user hbase tables
    upgradeUserTables(injector);

    // Upgrade all queue and stream tables.
    QueueAdmin queueAdmin = injector.getInstance(QueueAdmin.class);
    queueAdmin.upgrade();
  }

  private void upgradeSystemDatasets(Injector injector, DatasetFramework framework) throws Exception {

    // Upgrade all datasets in system namespace
    Id.Namespace systemNamespace = Id.Namespace.from(Constants.SYSTEM_NAMESPACE);
    for (DatasetSpecification spec : framework.getInstances(systemNamespace)) {
      System.out.println(String.format("Upgrading dataset: %s, spec: %s", spec.getName(), spec.toString()));
      DatasetAdmin admin = framework.getAdmin(Id.DatasetInstance.from(systemNamespace, spec.getName()), null);
      // we know admin is not null, since we are looping over existing datasets
      admin.upgrade();
      System.out.println(String.format("Upgraded dataset: %s", spec.getName()));
    }
    upgradeDatasetTypeMDS();
  }

  private void upgradeDatasetTypeMDS() throws TransactionFailureException,
    InterruptedException, IOException {
    LOG.info("YOO! In function");
    datasetTypeMDS.execute(new TransactionExecutor.Function<DatasetContext<Table>, Void>() {
      @Override
      public Void apply(DatasetContext<Table> ctx) throws Exception {
        Scanner rows = ctx.get().scan(null, null);
        Row row;
        while ((row = rows.next()) != null) {
          LOG.info("YOO! Inside While");
          String key = Bytes.toString(row.getRow()).trim();
          LOG.info("The key is {}", key);
          if (key.startsWith(DatasetTypeMDS.MODULES_PREFIX)) {
            LOG.info("Upgrading dataset type {} meta data", key);
            datasetModuleUpgrader(row);
          }
        }
        return null;
      }
    });
  }

  private void datasetModuleUpgrader(Row row) throws IOException, DatasetManagementException, URISyntaxException {
    DatasetTypeMDS datasetTypeMDS = new DatasetMetaTableUtil(namespacedFramework).getTypeMetaTable();
    DatasetModuleMeta datasetModuleMeta = GSON.fromJson(Bytes.toString(row.get(COLUMN)), DatasetModuleMeta.class);
    DatasetModuleMeta newDatasetModuleMeta;
    if (datasetModuleMeta.getJarLocation() == null) {
      LOG.info("YOO! Inside system");
      //system module
      newDatasetModuleMeta = new DatasetModuleMeta((Constants.SYSTEM_NAMESPACE + "_" +
        datasetModuleMeta.getName()), datasetModuleMeta.getClassName(), datasetModuleMeta.getJarLocation(),
                                                   datasetModuleMeta.getTypes(),
                                                   datasetModuleMeta.getUsesModules());
    } else {
      LOG.info("YOO! Inside user");
      String jarLocation = datasetModuleMeta.getJarLocation().toString();

      URI newJarLocation = new URI(jarLocation.substring(0, (jarLocation.lastIndexOf(CDAP_WITH_FORWARD_SLASH) +
        CDAP_WITH_FORWARD_SLASH.length())) + Constants.DEFAULT_NAMESPACE + FORWARD_SLASH +
        jarLocation.substring(jarLocation.lastIndexOf(CDAP_WITH_FORWARD_SLASH) + CDAP_WITH_FORWARD_SLASH.length()));
      
      newDatasetModuleMeta = new DatasetModuleMeta((Constants.DEFAULT_NAMESPACE + "_" + datasetModuleMeta.getName()),
                                                   datasetModuleMeta.getClassName(), newJarLocation,
                                                   datasetModuleMeta.getTypes(), datasetModuleMeta.getUsesModules());
      
    }
    datasetTypeMDS.writeModule(Id.Namespace.from(Constants.SYSTEM_NAMESPACE), newDatasetModuleMeta);
  }

  private static void upgradeUserTables(final Injector injector) throws Exception {
    // We assume that all tables in USER namespace belong to Table type datasets. So we loop thru them
    // and upgrading with the help of HBaseTableAdmin
    DefaultDatasetNamespace namespace = new DefaultDatasetNamespace(cConf);

    Configuration hConf = injector.getInstance(Configuration.class);
    HBaseAdmin hAdmin = new HBaseAdmin(hConf);
    final HBaseTableUtil hBaseTableUtil = injector.getInstance(HBaseTableUtil.class);

    for (HTableDescriptor desc : hAdmin.listTables()) {
      String tableName = desc.getNameAsString();
      TableId tableId = TableId.from(tableName);
      Id.DatasetInstance datasetInstanceId = Id.DatasetInstance.from(tableId.getNamespace(), tableId.getTableName());
      // todo: it works now, but we will want to change it if namespacing of datasets in HBase is more than +prefix
      if (namespace.fromNamespaced(datasetInstanceId) != null) {
        System.out.println(String.format("Upgrading hbase table: %s, desc: %s", tableName, desc.toString()));

        final boolean supportsIncrement =
          "true".equalsIgnoreCase(desc.getValue(Table.PROPERTY_READLESS_INCREMENT));
        DatasetAdmin admin = new AbstractHBaseDataSetAdmin(tableName, hConf, hBaseTableUtil) {
          @Override
          protected CoprocessorJar createCoprocessorJar() throws IOException {
            return HBaseTableAdmin.createCoprocessorJarInternal(cConf,
                                                                injector.getInstance(LocationFactory.class),
                                                                hBaseTableUtil,
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
        System.out.println(String.format("Upgraded hbase table: %s", tableName));
      }
    }
  }
}
