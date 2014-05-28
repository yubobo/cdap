package com.continuuity.hive.context;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.common.guice.LocationRuntimeModule;
import com.continuuity.common.guice.ZKClientModule;
import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.data2.dataset2.manager.DatasetManager;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.hive.client.HiveClient;
import com.continuuity.hive.client.NoOpHiveClient;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.twill.zookeeper.ZKClientService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stores/creates context for Hive queries to run in MapReduce jobs.
 */
public class ContextManager {
  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
  private static TransactionSystemClient txClient;
  private static DatasetManager datasetManager;

  public static void initialize(TransactionSystemClient txClient, DatasetManager datasetManager) {
    ContextManager.txClient = txClient;
    ContextManager.datasetManager = datasetManager;
    INITIALIZED.set(true);
  }

  public static TransactionSystemClient getTxClient(Configuration conf) {
    if (!INITIALIZED.get()) {
      selfInit(conf);
    }

    return txClient;
  }

  public static DatasetManager getDatasetManager(Configuration conf) {
    if (!INITIALIZED.get()) {
      selfInit(conf);
    }

    return datasetManager;
  }

  private static synchronized void selfInit(Configuration conf) {
    // Self init needs to happen only when running in as a MapReduce job.
    // In other cases, ContextManager will be initialized using initialize method.

    if (INITIALIZED.get()) {
      return;
    }

    CConfiguration cConf = CConfSerDe.deserialize(conf);
    Configuration hConf = HConfSerDe.deserialize(conf);

    Injector injector = Guice.createInjector(
      new ConfigModule(cConf, hConf),
      new ZKClientModule(),
      new LocationRuntimeModule().getDistributedModules(),
      new DiscoveryRuntimeModule().getDistributedModules(),
      new DataFabricModules(cConf, hConf).getDistributedModules(),
      new AbstractModule() {
        @Override
        protected void configure() {
          bind(HiveClient.class).to(NoOpHiveClient.class);
        }
      }
    );

    ZKClientService zkClientService = injector.getInstance(ZKClientService.class);
    // TODO: need to stop zkClientService at the end
    zkClientService.startAndWait();

    datasetManager = injector.getInstance(DatasetManager.class);
    txClient = injector.getInstance(TransactionSystemClient.class);

    INITIALIZED.set(true);
  }
}
