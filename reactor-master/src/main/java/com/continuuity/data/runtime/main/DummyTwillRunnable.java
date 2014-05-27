package com.continuuity.data.runtime.main;

import com.continuuity.api.metrics.Metrics;
import org.apache.twill.api.AbstractTwillRunnable;
import org.apache.twill.api.TwillContext;
import org.apache.twill.api.TwillRunnableSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DummyTwillRunnable extends AbstractTwillRunnable {
  private static final Logger LOG = LoggerFactory.getLogger(DummyTwillRunnable.class);
  private String name;
  private Metrics metrics;

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
    metrics.count("twill.metrics", 100);
    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      LOG.error("Twill Runnable interrupted from sleep");
    }
    LOG.info("Dummy Runnable Run ends");
  }
}
