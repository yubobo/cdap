package com.continuuity.hive.client.guice;

import com.continuuity.common.runtime.RuntimeModule;
import com.continuuity.hive.client.DistributedHiveCommandExecutor;
import com.continuuity.hive.client.HiveClient;
import com.continuuity.hive.client.HiveCommandExecutor;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Guice module for hive client.
 */
public class HiveClientModule extends RuntimeModule {

  @Override
  public Module getInMemoryModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(HiveClient.class).to(HiveCommandExecutor.class);
      }
    };
  }

  @Override
  public Module getSingleNodeModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(HiveClient.class).to(HiveCommandExecutor.class);
      }
    };
  }

  @Override
  public Module getDistributedModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(HiveClient.class).to(DistributedHiveCommandExecutor.class);
      }
    };
  }
}
