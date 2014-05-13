package com.continuuity.data2.datafabric.dataset.runtime;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.common.guice.IOModule;
import com.continuuity.common.guice.KafkaClientModule;
import com.continuuity.common.guice.LocationRuntimeModule;
import com.continuuity.common.guice.ZKClientModule;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.common.twill.AbstractReactorTwillRunnable;
import com.continuuity.data.stream.service.StreamHttpService;
import com.continuuity.data2.datafabric.dataset.service.DatasetUserHttpModule;
import com.continuuity.gateway.auth.AuthModule;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.twill.api.TwillContext;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.zookeeper.ZKClientService;

import java.util.List;

/**
 *
 */
public class DatasetUserRunnable extends AbstractReactorTwillRunnable {

  private Injector injector;

  public DatasetUserRunnable(String name, String cConfName, String hConfName) {
    super(name, cConfName, hConfName);
  }

  @Override
  public void initialize(TwillContext context) {
    super.initialize(context);

    try {
      CConfiguration cConf = getCConfiguration();
      Configuration hConf = getConfiguration();

      // Set the host name to the one provided by Twill
      cConf.set(Constants.Dataset.UserService.ADDRESS, context.getHost().getHostName());

      injector = Guice.createInjector(
        new DatasetUserHttpModule(),
        new ConfigModule(cConf, hConf),
        new IOModule(),
        new ZKClientModule(),
        new KafkaClientModule(),
        new DiscoveryRuntimeModule().getDistributedModules(),
        new LocationRuntimeModule().getDistributedModules(),
        new AuthModule());

    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected void getServices(List<? super Service> services) {
    services.add(injector.getInstance(ZKClientService.class));
    services.add(injector.getInstance(KafkaClientService.class));
    services.add(injector.getInstance(MetricsCollectionService.class));
    services.add(injector.getInstance(StreamHttpService.class));

  }
}