package com.continuuity.hive.client.guice;

import com.continuuity.hive.client.HiveClient;
import com.continuuity.hive.client.HiveCommandExecutor;
import com.continuuity.hive.client.HiveHttpCommandExecutor;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Guice module for hive client.
 */
public class HiveClientModule {

  /**
   * Use a thrift implementation for hive client.
   */
  public Module getBeelineThriftModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(HiveClient.class).to(HiveCommandExecutor.class);
      }
    };
  }

  /**
   * Use a thrift over http implementation for hive client.
   */
  public Module getBeelineThriftHttpModule() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(HiveClient.class).to(HiveHttpCommandExecutor.class);
      }
    };
  }
}
