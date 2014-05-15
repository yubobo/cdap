package com.continuuity.data2.transaction.runtime;

import com.continuuity.common.conf.CConfiguration;

import com.google.inject.Module;

/**
 *
 */
public class TransactionModules {
  private final CConfiguration cConf;

  public TransactionModules(CConfiguration cConf) {
    this.cConf = cConf;
  }

  public Module getInMemoryModules() {
    return new TransactionInMemoryModule(cConf);
  }

  public Module getSingleNodeModules() {
    return new TransactionLocalModule(cConf);
  }

  public Module getDistributedModules() {
    return new TransactionDistributedModule(cConf);
  }
}
