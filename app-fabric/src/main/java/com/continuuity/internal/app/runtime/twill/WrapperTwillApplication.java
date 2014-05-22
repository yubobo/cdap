package com.continuuity.internal.app.runtime.twill;

/*
import com.continuuity.internal.lang.ClassLoaders;
import com.google.common.collect.Maps;
import org.apache.twill.api.EventHandlerSpecification;
import org.apache.twill.api.LocalFile;
import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.RuntimeSpecification;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillContext;
import org.apache.twill.api.TwillRunnableSpecification;
import org.apache.twill.api.TwillSpecification;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;    */

/**
 *
 */
public class WrapperTwillApplication {

}
/*
public class WrapperTwillApplication implements TwillApplication {

  TwillApplication delegate;
  Map<String, RuntimeSpecification> runSpec;

  WrapperTwillApplication(TwillApplication delegate) {
    this.delegate = delegate;
  }

  @Override
  public TwillSpecification configure() {
    final TwillSpecification spec = delegate.configure();

    TwillSpecification newspec = new TwillSpecification() {
      @Override
      public String getName() {
        return spec.getName();
      }

      @Override
      public Map<String, RuntimeSpecification> getRunnables() {
        Map<String, RuntimeSpecification>  runnables = spec.getRunnables();
        runSpec = Maps.newHashMap();
        for(final Map.Entry<String, RuntimeSpecification> runnable : runnables.entrySet()) {
          runSpec.put(runnable.getKey(), runnable.getValue());
          runnables.put(runnable.getKey(), new RuntimeSpecification() {
            @Override
            public String getName() {
              return runnable.getValue().getName();
            }

            @Override
            public TwillRunnableSpecification getRunnableSpecification() {
              return new TwillRunnableSpecification() {
                @Override
                public String getClassName() {
                  //should we rewrite this class name here?
                  return runnable.getValue().getRunnableSpecification().getClassName();

                }

                @Override
                public String getName() {
                  return runnable.getValue().getRunnableSpecification().getName();
                }

                @Override
                public Map<String, String> getConfigs() {
                  Map<String, String> newMap = Maps.newHashMap();
                  newMap.put("reactor.class.name", this.getClassName());
                  return newMap;
                }
              };
            }

            @Override
            public ResourceSpecification getResourceSpecification() {
              return null;
            }

            @Override
            public Collection<LocalFile> getLocalFiles() {
              return null;
            }
          });
        }
        return runSpec;
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
    return newspec;
  }


  public void initialize(TwillContext context) {
    // get the specification, load the class and call initialize on it
    // then do field visit for metrics
    TwillRunnableSpecification specification = context.getSpecification();
    Map<String,String> runnableConfigs = specification.getConfigs();

    for(Map.Entry<String, RuntimeSpecification> runnable : runSpec.entrySet()){
      try {
        Class obj = ClassLoaders.loadClass(runnable.getValue().getName(), null, this);
        // Field visitor to add the metrics
        // how to implement metrics, metrics collector service ?
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

  }

}
*/
