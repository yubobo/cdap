package com.continuuity.data2.dataset2.lib.table.proxy;

import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillRunner;
import org.apache.twill.discovery.DiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DatasetAdminYarnProxy extends DatasetAdminProxy {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetAdminYarnProxy.class);

  private final TwillRunner twillRunner;
  private final TwillController twillController;
  private final String user;
  private final DiscoveryService discoveryService;

  public DatasetAdminYarnProxy(DiscoveryService discoveryService, String user, TwillRunner twillRunner) {
    this.discoveryService = discoveryService;
    this.user = user;
    this.twillRunner = twillRunner;
    this.twillController = twillRunner
      .prepare(new DatasetUserRunnable())
      .withApplicationArguments(user).start();
  }

  @Override
  public String getHostAndPort() {
    // TODO: host should be determined from twillController
    return "localhost:10010";
  }

  @Override
  public void cleanup() {
    twillController.stopAndWait();
  }
}
