package com.continuuity.data2.transaction.runtime;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.data2.transaction.distributed.TransactionServiceClient;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.data2.transaction.persist.HDFSTransactionStateStorage;
import com.continuuity.data2.transaction.persist.NoOpTransactionStateStorage;
import com.continuuity.data2.transaction.persist.TransactionStateStorage;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 *
 */
public class TransactionDistributedModule extends AbstractModule {
  private final CConfiguration conf;

  public TransactionDistributedModule(CConfiguration cConf) {
    this.conf = cConf;
  }

  @Override
  protected void configure() {
    if (conf.getBoolean(Constants.Transaction.Manager.CFG_DO_PERSIST, true)) {
      bind(TransactionStateStorage.class).to(HDFSTransactionStateStorage.class).in(Singleton.class);
    } else {
      bind(TransactionStateStorage.class).to(NoOpTransactionStateStorage.class).in(Singleton.class);
    }
    bind(InMemoryTransactionManager.class).in(Singleton.class);
    bind(TransactionSystemClient.class).to(TransactionServiceClient.class).in(Singleton.class);
  }
}
