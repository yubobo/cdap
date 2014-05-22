package com.continuuity.data.runtime.main;

import com.continuuity.logging.run.DummyTwillRunnable;
import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillSpecification;

/**
 *
 */
public class DummyTwillApplication implements TwillApplication {
  @Override
  public TwillSpecification configure() {
    addDummyService(TwillSpecification.Builder.with().setName("dummy-app").withRunnable());
    return null;
  }

  private TwillSpecification.Builder.RunnableSetter addDummyService(TwillSpecification.Builder.MoreRunnable
                                                                         builder) {

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
