package com.continuuity.hive.client;

import com.google.inject.Inject;
import org.apache.twill.discovery.DiscoveryServiceClient;

/**
 * Hive client using beeline with thrift over http.
 */
public class HiveHttpCommandExecutor extends AbstractBeelineCommandExecutor {

  @Inject
  public HiveHttpCommandExecutor(DiscoveryServiceClient discoveryClient) {
    super(discoveryClient);
  }

  @Override
  protected String getConnectionPostfix() {
    return "/default;auth=noSasl?" +
           "hive.server2.transport.mode=http;hive.server2.thrift.http.path=cliservice;" +
           "hive.exec.pre.hooks=com.continuuity.hive.hooks.TransactionPreHook;" +
           "hive.exec.post.hooks=com.continuuity.hive.hooks.TransactionPostHook";
  }
}
