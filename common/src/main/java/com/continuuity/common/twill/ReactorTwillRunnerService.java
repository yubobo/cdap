package com.continuuity.common.twill;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.RunId;
import org.apache.twill.api.SecureStoreUpdater;
import org.apache.twill.api.TwillApplication;
import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillPreparer;
import org.apache.twill.api.TwillRunnable;
import org.apache.twill.api.TwillRunnerService;
import org.apache.twill.common.Cancellable;
import org.apache.twill.yarn.YarnTwillRunnerService;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * A {@link TwillRunnerService} that delegates to {@link YarnTwillRunnerService} and by default always
 * set the process user name based on CConfiguration when prepare to launch application.
 */
@Singleton
public final class ReactorTwillRunnerService implements TwillRunnerService {

  private final YarnTwillRunnerService delegate;
  private final String yarnUser;

  @Inject
  ReactorTwillRunnerService(YarnTwillRunnerService delegate, CConfiguration cConf) {
    this.delegate = delegate;
    this.yarnUser = cConf.get(Constants.CFG_YARN_USER, System.getProperty("user.name"));
  }

  @Override
  public ListenableFuture<State> start() {
    return delegate.start();
  }

  @Override
  public State startAndWait() {
    return Futures.getUnchecked(start());
  }

  @Override
  public boolean isRunning() {
    return delegate.isRunning();
  }

  @Override
  public State state() {
    return delegate.state();
  }

  @Override
  public ListenableFuture<State> stop() {
    return delegate.stop();
  }

  @Override
  public State stopAndWait() {
    return Futures.getUnchecked(stop());
  }

  @Override
  public void addListener(Listener listener, Executor executor) {
    delegate.addListener(listener, executor);
  }

  @Override
  public TwillPreparer prepare(TwillRunnable runnable) {
    return delegate.prepare(runnable).setUser(yarnUser);
  }

  @Override
  public Cancellable scheduleSecureStoreUpdate(SecureStoreUpdater updater, long initialDelay,
                                               long delay, TimeUnit unit) {
    return delegate.scheduleSecureStoreUpdate(updater, initialDelay, delay, unit);
  }

  @Override
  public TwillPreparer prepare(TwillRunnable runnable, ResourceSpecification resourceSpecification) {
    return delegate.prepare(runnable, resourceSpecification).setUser(yarnUser);
  }

  @Override
  public TwillPreparer prepare(TwillApplication application) {
    return delegate.prepare(application).setUser(yarnUser);
  }

  @Override
  public TwillController lookup(String applicationName, RunId runId) {
    return delegate.lookup(applicationName, runId);
  }

  @Override
  public Iterable<TwillController> lookup(String applicationName) {
    return delegate.lookup(applicationName);
  }

  @Override
  public Iterable<LiveInfo> lookupLive() {
    return delegate.lookupLive();
  }
}