package com.continuuity.data2.dataset2.lib.table.proxy;

import com.continuuity.data2.dataset2.manager.DatasetManager;
import com.google.common.util.concurrent.AbstractIdleService;

/**
 * Runs within a container that is running as a particular user.
 * Does various things on behalf of a user, including {@link DatasetUserAdminHandler} operations.
 */
public class DatasetUserService extends AbstractIdleService {

  private final String user;
  private DatasetManager client;

  public DatasetUserService(String user, DatasetManager client) {
    this.user = user;
    this.client = client;

    DatasetUserAdminHandler handler = new DatasetUserAdminHandler(client);
  }

  @Override
  protected void startUp() throws Exception {

  }

  @Override
  protected void shutDown() throws Exception {

  }
}
