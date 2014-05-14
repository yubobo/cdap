package com.continuuity.data2.transaction.runtime;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.data2.transaction.inmemory.InMemoryTxSystemClient;
import com.continuuity.data2.transaction.persist.LocalFileTransactionStateStorage;
import com.continuuity.data2.transaction.persist.NoOpTransactionStateStorage;
import com.continuuity.data2.transaction.persist.TransactionStateStorage;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 *
 */
public class TransactionLocalModule extends AbstractModule {
  private final CConfiguration cConf;

  public TransactionLocalModule(CConfiguration cConf) {
    this.cConf = cConf;
  }

  @Override
  protected void configure() {
    bind(InMemoryTransactionManager.class).in(Singleton.class);
    bind(TransactionSystemClient.class).to(InMemoryTxSystemClient.class).in(Singleton.class);
    if (cConf.getBoolean(Constants.Transaction.Manager.CFG_DO_PERSIST, true)) {
      bind(TransactionStateStorage.class).to(LocalFileTransactionStateStorage.class).in(Singleton.class);
    } else {
      bind(TransactionStateStorage.class).to(NoOpTransactionStateStorage.class).in(Singleton.class);
    }

    bind(CConfiguration.class).annotatedWith(Names.named("TransactionServerConfig")).toInstance(cConf);
  }
}
