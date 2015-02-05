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

package co.cask.cdap.explore;

import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.guice.ConfigModule;
import co.cask.cdap.common.guice.DiscoveryRuntimeModule;
import co.cask.cdap.common.guice.IOModule;
import co.cask.cdap.common.guice.KafkaClientModule;
import co.cask.cdap.common.guice.LocationRuntimeModule;
import co.cask.cdap.common.guice.ZKClientModule;
import co.cask.cdap.data.runtime.DataFabricModules;
import co.cask.cdap.data.runtime.DataSetsModules;
import co.cask.cdap.data.stream.StreamAdminModules;
import co.cask.cdap.explore.executor.ExploreExecutorService;
import co.cask.cdap.explore.guice.ExploreClientModule;
import co.cask.cdap.explore.guice.ExploreRuntimeModule;
import co.cask.cdap.gateway.auth.AuthModule;
import co.cask.cdap.logging.guice.LoggingModules;
import co.cask.cdap.metrics.guice.MetricsClientRuntimeModule;
import co.cask.cdap.notifications.feeds.client.NotificationFeedClientModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.zookeeper.ZKClientService;
import org.junit.Test;

/**
 *
 */
public class ExploreTest {

  @Test
  public void test() throws Exception {
    CConfiguration cConfiguration = CConfiguration.create();
    cConfiguration.clear();
    cConfiguration.addResource(this.getClass().getResource("/cConf.xml"));

    Configuration hConf = HBaseConfiguration.create();
    hConf.clear();
    hConf.addResource(this.getClass().getResource("/hConf.xml"));

    Injector injector = Guice.createInjector(
      new ConfigModule(cConfiguration, hConf),
      new IOModule(), new ZKClientModule(),
      new KafkaClientModule(),
      new MetricsClientRuntimeModule().getDistributedModules(),
      new DiscoveryRuntimeModule().getDistributedModules(),
      new LocationRuntimeModule().getDistributedModules(),
      new DataFabricModules().getDistributedModules(),
      new DataSetsModules().getDistributedModule(),
      new LoggingModules().getDistributedModules(),
      new ExploreRuntimeModule().getDistributedModules(),
      new ExploreClientModule(),
      new StreamAdminModules().getDistributedModules(),
      new NotificationFeedClientModule(),
      new AuthModule());

    injector.getInstance(ZKClientService.class).startAndWait();
    injector.getInstance(KafkaClientService.class).startAndWait();
    ExploreExecutorService exploreService = injector.getInstance(ExploreExecutorService.class);
    exploreService.startAndWait();
  }

}
