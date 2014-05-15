package com.continuuity.data2.dataset2.lib.table.proxy;

/**
 *
 */
public class DatasetAdminProxyClient {

  /**
   *
   * @param user
   * @return
   */
  public DatasetAdminProxy getDatasetAdminProxy(String user) {
    // discover singleton service {@link DatasetAdminProxyManager}
    // ask for the address that a {@link DatasetUserAdminService} is running for the user
    //   ({@link DatasetUserAdminService} will spin one up if nothing is yet running for the provided user)
    throw new UnsupportedOperationException("TODO");
  }

}
