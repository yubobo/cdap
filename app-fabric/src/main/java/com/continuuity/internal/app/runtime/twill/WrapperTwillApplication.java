package com.continuuity.internal.app.runtime.twill;


import com.continuuity.api.metrics.Metrics;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.common.metrics.MetricsScope;
import com.continuuity.internal.app.runtime.MetricsFieldSetter;
import com.continuuity.internal.lang.ClassLoaders;
import com.continuuity.internal.lang.Reflections;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.Reflection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 *
 */
public class WrapperTwillApplication implements TwillApplication {

  private static final Logger LOG = LoggerFactory.getLogger(WrapperTwillApplication.class);
  TwillApplication delegate;
  Map<String, RuntimeSpecification> runSpec;

  /**
   *
   * @param delegate
   */
  public WrapperTwillApplication(TwillApplication delegate) {
    this.delegate = delegate;
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

    @Inject
    WrapperTwillRunnable(MetricsCollectionService metricsCollectionService) {
      this.collectionService = metricsCollectionService;
    }

    @Override
    public TwillRunnableSpecification configure() {
      LOG.info("Configuring WrapperTwillRunnable");
      return TwillRunnableSpecification.Builder.with()
        .setName("wrapper")
        .noConfigs()
        .build();
    }

    @Override
    public void initialize(TwillContext context) {
      LOG.info("Initializing in WrapperTwillRunnable");
      TwillRunnableSpecification spec = context.getSpecification();
      //runtime specification of user twill runnable
      final String className = spec.getConfigs().get("reactor.class.name");
      LOG.info("User Twill Runnable className is : {}", className);
      try {
        Class obj = Class.forName(className);
        delegate = (TwillRunnable) obj.newInstance();
        Reflections.visit(delegate, TypeToken.of(delegate.getClass()), new MetricsFieldSetter(new Metrics() {

          @Override
          public void count(String counterName, int delta) {
            collectionService.getCollector(MetricsScope.USER,
                                           String.format("%s.t", className), "0").gauge(counterName, delta);
          }
        }));
        delegate.configure();

        //try to inject metrics here
        //can we pass the same context?
        delegate.initialize(context);
      } catch (ClassNotFoundException e) {
        LOG.error(e.getMessage(), e);
      } catch (InstantiationException e) {
        LOG.error(e.getMessage(), e);
      } catch (IllegalAccessException e) {
        LOG.error(e.getMessage(), e);
      }
    }

    @Override
    public void handleCommand(Command command) throws Exception {
      delegate.handleCommand(command);
    }

    @Override
    public void stop() {
      delegate.stop();
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


