package com.continuuity.common.twill;

import com.google.common.util.concurrent.AbstractIdleService;
import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.RunId;
import org.apache.twill.api.SecureStoreUpdater;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillPreparer;
import org.apache.twill.api.TwillRunnable;
import org.apache.twill.api.TwillRunnerService;
import org.apache.twill.common.Cancellable;

import java.util.concurrent.TimeUnit;

/**
 * In-memory implementation of {@link TwillRunnerService}.
 */
public class InMemoryTwillRunnerService extends AbstractIdleService implements TwillRunnerService {

  // TODO: implementation

  @Override
  protected void startUp() throws Exception {

  }

  @Override
  protected void shutDown() throws Exception {

  }

  @Override
  public TwillPreparer prepare(TwillRunnable runnable) {
    return null;
  }

  @Override
  public TwillPreparer prepare(TwillRunnable runnable, ResourceSpecification resourceSpecification) {
    return null;
  }

  @Override
  public TwillPreparer prepare(TwillApplication application) {
    return null;
  }

  @Override
  public TwillController lookup(String applicationName, RunId runId) {
    return null;
  }

  @Override
  public Iterable<TwillController> lookup(String applicationName) {
    return null;
  }

  @Override
  public Iterable<LiveInfo> lookupLive() {
    return null;
  }

  @Override
  public Cancellable scheduleSecureStoreUpdate(SecureStoreUpdater updater, long initialDelay, long delay, TimeUnit unit) {
    return null;
  }
}
