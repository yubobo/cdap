/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.common.guice;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.runtime.RuntimeModule;
import com.continuuity.common.twill.InMemoryTwillRunnerService;
import com.continuuity.common.twill.ReactorTwillRunnerService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.twill.api.TwillRunner;
import org.apache.twill.api.TwillRunnerService;
import org.apache.twill.filesystem.LocationFactories;
import org.apache.twill.filesystem.LocationFactory;
import org.apache.twill.yarn.YarnTwillRunnerService;

/**
 * Guice module for providing bindings for Twill. This module requires accessible bindings to
 * {@link CConfiguration}, {@link YarnConfiguration} and {@link LocationFactory}.
 */
public class TwillModule extends RuntimeModule {

  @Override
  public Module getInMemoryModules() {
    return new InMemoryTwillModule();
  }

  @Override
  public Module getSingleNodeModules() {
    return new InMemoryTwillModule();
  }

  @Override
  public Module getDistributedModules() {
    return new YarnTwillModule();
  }

  /**
   * In-memory version of {@link TwillModule} that uses {@link InMemoryTwillRunnerService}.
   */
  private static final class InMemoryTwillModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(TwillRunnerService.class).to(InMemoryTwillRunnerService.class).in(Scopes.SINGLETON);
      bind(TwillRunner.class).to(TwillRunnerService.class);
    }
  }

  /**
   * Distributed (YARN) version of {@link TwillModule} that uses {@link ReactorTwillRunnerService}.
   */
  private static final class YarnTwillModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(TwillRunnerService.class).to(ReactorTwillRunnerService.class).in(Scopes.SINGLETON);
      bind(TwillRunner.class).to(TwillRunnerService.class);
    }

    /**
     * Provider method for instantiating {@link YarnTwillRunnerService}.
     */
    @Singleton
    @Provides
    private YarnTwillRunnerService provideYarnTwillRunnerService(CConfiguration configuration,
      YarnConfiguration yarnConfiguration,
      LocationFactory locationFactory) {
      String zkConnectStr = configuration.get(Constants.Zookeeper.QUORUM) +
        configuration.get(Constants.CFG_TWILL_ZK_NAMESPACE, "/weave");

      // Copy the yarn config and set the max heap ratio.
      YarnConfiguration yarnConfig = new YarnConfiguration(yarnConfiguration);
      yarnConfig.set(Constants.CFG_TWILL_RESERVED_MEMORY_MB, configuration.get(Constants.CFG_TWILL_RESERVED_MEMORY_MB));
      YarnTwillRunnerService runner = new YarnTwillRunnerService(yarnConfig,
                                                                 zkConnectStr,
                                                                 LocationFactories.namespace(locationFactory, "weave"));

      // Set JVM options based on configuration
      runner.setJVMOptions(configuration.get(Constants.AppFabric.PROGRAM_JVM_OPTS));

      return runner;
    }
  }


}
