package com.continuuity.internal.app.runtime.twill;

import org.apache.twill.api.AbstractTwillRunnable;
import org.apache.twill.api.TwillContext;
import org.apache.twill.api.TwillRunnableSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DummyTwillRunnable extends AbstractTwillRunnable {
  private static final Logger LOG = LoggerFactory.getLogger(DummyTwillRunnable.class);
  private String name;

  public DummyTwillRunnable(String name) {
    this.name = name;
  }
  public DummyTwillRunnable() {
    this.name = "dummy";
  }
  @Override
  public TwillRunnableSpecification configure() {
    return TwillRunnableSpecification.Builder.with()
      .setName("hello")
      .noConfigs()
      .build();
  }

  @Override
  public void initialize(TwillContext context) {
    super.initialize(context);
    LOG.info("Dummy Runnable initialized in context : " + context.getInstanceId());
  }

  @Override
  public void stop() {
    LOG.info("Dummy Runnable Stopped");
  }

  @Override
  public void run() {
    LOG.info("Dummy Runnable Started");
  }
}
