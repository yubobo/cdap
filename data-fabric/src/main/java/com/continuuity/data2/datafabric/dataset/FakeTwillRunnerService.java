package com.continuuity.data2.datafabric.dataset;

import com.google.common.util.concurrent.Service;
import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.RunId;
import org.apache.twill.api.SecureStoreUpdater;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillPreparer;
import org.apache.twill.api.TwillRunnable;
import org.apache.twill.api.TwillRunnerService;
import org.apache.twill.common.Cancellable;
import org.apache.twill.filesystem.Location;
import org.apache.twill.internal.AbstractTwillService;

import java.util.concurrent.TimeUnit;

/**
 * TODO(alvin): ???
 */
public class FakeTwillRunnerService extends AbstractTwillService implements TwillRunnerService {

  protected FakeTwillRunnerService(Location applicationLocation) {
    super(applicationLocation);
  }

  @Override
  protected Service getServiceDelegate() {
    return null;
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
