package com.continuuity.data2.dataset2.lib.table.proxy;

import com.google.common.util.concurrent.AbstractIdleService;

/**
 *
 */
public class DatasetAdminProxyManager extends AbstractIdleService {


  @Override
  protected void startUp() throws Exception {
    // start HTTP service that handles requests from {@link DatasetAdminProxyClient}
  }

  @Override
  protected void shutDown() throws Exception {

  }
}
