package com.continuuity.data2.dataset2.lib.table.proxy;

/**
 * Client for {@link DatasetAdminProxyManager}.
 */
public class DatasetAdminProxyClient {

  /**
   * Gets a {@DatasetAdminProxy} using {@link DatasetAdminProxyManager} via REST.
   *
   * @param user
   * @return
   */
  public DatasetAdminProxy getDatasetAdminProxy(String user, String instanceName) {
    // TODO
    return new DatasetAdminProxyManager().getProxy(user, instanceName);
  }

}
