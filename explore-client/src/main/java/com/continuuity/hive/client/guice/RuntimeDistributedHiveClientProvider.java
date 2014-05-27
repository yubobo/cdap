package com.continuuity.hive.client.guice;

import com.continuuity.hive.client.DistributedHiveCommandExecutor;
import com.continuuity.hive.client.HiveClient;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.twill.discovery.DiscoveryServiceClient;

/**
 *
 */
public class RuntimeDistributedHiveClientProvider implements Provider<HiveClient> {

  private final DiscoveryServiceClient discoveryClient;

  @Inject
  public RuntimeDistributedHiveClientProvider(DiscoveryServiceClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  @Override
  public HiveClient get() {
    return new DistributedHiveCommandExecutor(discoveryClient);
  }
}
