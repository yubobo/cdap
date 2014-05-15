package com.continuuity.data2.dataset2.lib.table.proxy;

import com.continuuity.data2.datafabric.dataset.client.DatasetManagerServiceClient;
import com.google.common.base.Preconditions;
import org.apache.twill.api.AbstractTwillRunnable;
import org.apache.twill.api.TwillContext;

/**
 * Executes user code on behalf of a particular user inside
 * a container running as that user. For security.
 */
public class DatasetUserRunnable extends AbstractTwillRunnable {

  private String user;
  private DatasetManagerServiceClient client;

  @Override
  public void initialize(TwillContext context) {
    super.initialize(context);

    Preconditions.checkArgument(context.getApplicationArguments().length == 1);
    doInitialize(context.getApplicationArguments()[0]);
  }

  public void doInitialize(String user) {
    this.user = user;
  }

  @Override
  public void stop() {
    // TODO
  }

  @Override
  public void run() {
    // TODO: start http service
  }
}
