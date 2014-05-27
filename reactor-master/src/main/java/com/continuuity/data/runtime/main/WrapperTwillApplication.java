package com.continuuity.data.runtime.main;


import com.continuuity.api.metrics.Metrics;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.common.guice.IOModule;
import com.continuuity.common.guice.KafkaClientModule;
import com.continuuity.common.guice.LocationRuntimeModule;
import com.continuuity.common.guice.ZKClientModule;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.common.metrics.MetricsScope;
import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.gateway.auth.AuthModule;
import com.continuuity.internal.app.runtime.MetricsFieldSetter;
import com.continuuity.internal.lang.Reflections;
import com.continuuity.logging.guice.LoggingModules;
import com.continuuity.metrics.guice.MetricsClientRuntimeModule;
import com.continuuity.metrics.guice.MetricsHandlerModule;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.twill.api.Command;
import org.apache.twill.api.EventHandlerSpecification;
import org.apache.twill.api.LocalFile;
import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.RuntimeSpecification;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillContext;
import org.apache.twill.api.TwillRunnable;
import org.apache.twill.api.TwillRunnableSpecification;
import org.apache.twill.api.TwillSpecification;
import org.apache.twill.common.Services;
import org.apache.twill.internal.DefaultLocalFile;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.zookeeper.ZKClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

/**
 *
 */
public class WrapperTwillApplication implements TwillApplication {

  private static final Logger LOG = LoggerFactory.getLogger(WrapperTwillApplication.class);
  TwillApplication delegate;
  Map<String, RuntimeSpecification> runSpec;
  private final File cConfFile;
  private final File hConfFile;

  /**
   *
   * @param delegate
   */
  public WrapperTwillApplication(TwillApplication delegate, File cConfFile, File hConfFile) {
    this.delegate = delegate;
    this.cConfFile = cConfFile;
    this.hConfFile = hConfFile;
  }

  /**
   *
   * @return
   */
  @Override
  public TwillSpecification configure() {
    LOG.info("Configuring the Wrapper Twill Application");
    final TwillSpecification spec = delegate.configure();


    TwillSpecification newspec = new TwillSpecification() {
      @Override
      public String getName() {
        return spec.getName();
      }

      @Override
      public Map<String, RuntimeSpecification> getRunnables() {
        LOG.info("WrapperTwillApplication: getRunnables started");
        Map<String, RuntimeSpecification>  runnables = spec.getRunnables();
        Map<String, RuntimeSpecification> newRunnables = Maps.newHashMap();
        runSpec = Maps.newHashMap();
        for (final Map.Entry<String, RuntimeSpecification> runnable : runnables.entrySet()) {
          runSpec.put(runnable.getKey(), runnable.getValue());
          newRunnables.put(runnable.getKey(), new RuntimeSpecification() {
            @Override
            public String getName() {
              return runnable.getValue().getName();
            }

            @Override
            public TwillRunnableSpecification getRunnableSpecification() {
              return new TwillRunnableSpecification() {
                @Override
                public String getClassName() {
                  return WrapperTwillRunnable.class.getName();
                }

                @Override
                public String getName() {
                  return runnable.getValue().getRunnableSpecification().getName();
                }

                @Override
                public Map<String, String> getConfigs() {
                  Map<String, String> newMap = Maps.newHashMap();
                  newMap.put("reactor.class.name", runnable.getValue().getRunnableSpecification().getClassName());
                  newMap.put("hConf", "hConf.xml");
                  newMap.put("cConf", "cConf.xml");
                  return newMap;
                }
              };
            }

            @Override
            public ResourceSpecification getResourceSpecification() {
              return runnable.getValue().getResourceSpecification();
            }

            @Override
            public Collection<LocalFile> getLocalFiles() {
              Collection<LocalFile> files = Lists.newArrayList();
              files.add(new DefaultLocalFile("hConf.xml", hConfFile.toURI(), -1, -1, false, null));
              files.add(new DefaultLocalFile("cConf.xml", cConfFile.toURI(), -1, -1, false, null));

              for (LocalFile file : runnable.getValue().getLocalFiles()) {
                files.add(file);
              }
              return files;
            }
          });
        }
        return newRunnables;
      }

      @Override
      public List<Order> getOrders() {
        return spec.getOrders();
      }

      @Nullable
      @Override
      public EventHandlerSpecification getEventHandler() {
        return spec.getEventHandler();
      }
    };

    LOG.info("WrapperTwillApplication: configure finished");
    return newspec;

     // add new WrapperTwillRunnable here with the builder
  }


  class WrapperTwillRunnable implements TwillRunnable {
    TwillRunnable delegate;
    MetricsCollectionService collectionService;
    private ZKClientService zkClientService;
    private KafkaClientService kafkaClientService;
    private SettableFuture<?> completion;


    @Override
    public TwillRunnableSpecification configure() {
      LOG.info("Configuring WrapperTwillRunnable");
      return TwillRunnableSpecification.Builder.with().setName("wrapper").noConfigs().build();
    }

    @Override
    public void initialize(TwillContext context) {
      LOG.info("Initializing in WrapperTwillRunnable");
      TwillRunnableSpecification spec = context.getSpecification();
      Map<String, String> configs = spec.getConfigs();
      //runtime specification of user twill runnable
      final String className = configs.get("reactor.class.name");


      LOG.info("User Twill Runnable className is : {}", className);
      try {

        Configuration hConf = new Configuration();
        hConf.clear();
        hConf.addResource(new File(configs.get("hConf")).toURI().toURL());

        UserGroupInformation.setConfiguration(hConf);

        CConfiguration cConf = CConfiguration.create();
        cConf.clear();
        cConf.addResource(new File(configs.get("cConf")).toURI().toURL());
        Injector injector = createGuiceInjector(cConf, hConf);
        collectionService = injector.getInstance(MetricsCollectionService.class);
        zkClientService = injector.getInstance(ZKClientService.class);
        kafkaClientService = injector.getInstance(KafkaClientService.class);
        completion = SettableFuture.create();
        Futures.getUnchecked(Services.chainStart(zkClientService, kafkaClientService, collectionService));
        LOG.info("Starting Wrapper Services");
        Class obj = Class.forName(className);
        delegate = (TwillRunnable) obj.newInstance();
        Reflections.visit(delegate, TypeToken.of(delegate.getClass()), new MetricsFieldSetter(new Metrics() {

          @Override
          public void count(String counterName, int delta) {
            LOG.info("collectionService status:" + collectionService.isRunning());
            collectionService.getCollector(MetricsScope.USER,
                                           String.format("HelloWorld.f.WhoFlow.saver"), "0").
                                                          gauge(counterName, delta);

          }
        }));
        delegate.configure();

        delegate.initialize(context);
        //completion.get();
        LOG.info("Wrapper Services Stopped");

      } catch (ClassNotFoundException e) {
        LOG.error(e.getMessage(), e);
        Throwables.propagate(e);
      } catch (InstantiationException e) {
        LOG.error(e.getMessage(), e);
        Throwables.propagate(e);
      } catch (IllegalAccessException e) {
        LOG.error(e.getMessage(), e);
        Throwables.propagate(e);
      } catch (MalformedURLException e) {
        LOG.error(e.getMessage(), e);
        Throwables.propagate(e);
      }
    }

    private Injector createGuiceInjector(CConfiguration cConf, Configuration hConf) {
      return Guice.createInjector(new ConfigModule(cConf, hConf), new IOModule(),
                                  new ZKClientModule(),
                                  new KafkaClientModule(),
                                  new LocationRuntimeModule().getDistributedModules(),
                                  new DiscoveryRuntimeModule().getDistributedModules(),
                                  new AuthModule(),
                                  new MetricsClientRuntimeModule().getDistributedModules());
    }

    @Override
    public void handleCommand(Command command) throws Exception {
      delegate.handleCommand(command);
    }

    @Override
    public void stop() {
      delegate.stop();
      Futures.getUnchecked(Services.chainStop(collectionService, kafkaClientService, zkClientService));
    }

    @Override
    public void destroy() {
      delegate.destroy();
    }

    @Override
    public void run() {
      LOG.info("Running in Wrapper Twill Runnable");
      delegate.run();
    }
  }
}
