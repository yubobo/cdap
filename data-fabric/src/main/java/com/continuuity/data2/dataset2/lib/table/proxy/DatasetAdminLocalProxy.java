package com.continuuity.data2.dataset2.lib.table.proxy;

import org.apache.twill.discovery.DiscoveryService;

/**
 *
 */
public class DatasetAdminLocalProxy extends DatasetAdminProxy {

  private final DatasetUserRunnable runnable;
  private final Thread runnableThread;
  private final DiscoveryService discoveryService;

  public DatasetAdminLocalProxy(DiscoveryService discoveryService, String user) {
    this.discoveryService = discoveryService;
    runnable = new DatasetUserRunnable();
    runnable.doInitialize(user);
    runnableThread = new Thread(runnable);
    runnableThread.start();
  }

  @Override
  public String getHostAndPort() {
    return "localhost:10010";
  }

  @Override
  public void cleanup() {
    runnable.stop();
  }
}
