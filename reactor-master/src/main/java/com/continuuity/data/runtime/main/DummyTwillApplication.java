package com.continuuity.data.runtime.main;

import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DummyTwillApplication implements TwillApplication {
  private static final Logger LOG = LoggerFactory.getLogger(DummyTwillApplication.class);

  @Override
  public TwillSpecification configure() {
    LOG.info("DummyTwillApplication configure");
    return addDummyService(TwillSpecification.Builder.with().setName("dummy-app").withRunnable()).anyOrder().build();
  }

  private TwillSpecification.Builder.RunnableSetter addDummyService(TwillSpecification.Builder.MoreRunnable
                                                                         builder) {
    LOG.info("DummyTwillApplication runnable added");
    int numInstances =  1;
    int memory = 1024;

    ResourceSpecification spec = ResourceSpecification.Builder
      .with()
      .setVirtualCores(2)
      .setMemory(memory, ResourceSpecification.SizeUnit.MEGA)
      .setInstances(numInstances)
      .build();

    return builder.add(new DummyTwillRunnable("dummy"), spec)
      .noLocalFiles();
  }
}
