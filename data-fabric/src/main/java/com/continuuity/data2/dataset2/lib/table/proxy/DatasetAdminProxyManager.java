package com.continuuity.data2.dataset2.lib.table.proxy;

import com.continuuity.common.conf.Constants;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Provides {@link DatasetAdminProxy} instances, spinning up a {@link DatasetUserService}
 * if none yet exist for the specified user.
 */
public class DatasetAdminProxyManager {

  private Map<String, InetSocketAddress> userServices;

  public DatasetAdminProxy getProxy(String user, String instanceName) {
    InetSocketAddress address = getOrCreateUserService(user);
    return new DatasetAdminProxy(address, instanceName);
  }

  private InetSocketAddress getOrCreateUserService(String user) {
    InetSocketAddress existingService = userServices.get(user);
    if (existingService == null) {
      // TODO: launch new thread (local) or YARN container (distributed) running {@link DatasetUserService}
      //       and obtain address
      return new InetSocketAddress("localhost", Constants.Dataset.User.DEFAULT_PORT);
    }
    return existingService;
  }

}
