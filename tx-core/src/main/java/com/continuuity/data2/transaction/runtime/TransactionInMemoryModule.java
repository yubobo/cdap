package com.continuuity.data2.transaction.runtime;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.data2.transaction.inmemory.InMemoryTxSystemClient;
import com.continuuity.data2.transaction.persist.NoOpTransactionStateStorage;
import com.continuuity.data2.transaction.persist.TransactionStateStorage;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 *
 */
public class TransactionInMemoryModule extends AbstractModule {
  private final CConfiguration cConf;

  public TransactionInMemoryModule(CConfiguration cConf) {
    this.cConf = cConf;
  }

  @Override
  protected void configure() {
    bind(TransactionStateStorage.class).to(NoOpTransactionStateStorage.class).in(Singleton.class);
    bind(InMemoryTransactionManager.class).in(Singleton.class);
    bind(TransactionSystemClient.class).to(InMemoryTxSystemClient.class).in(Singleton.class);
    bind(CConfiguration.class).annotatedWith(Names.named("TransactionServerConfig")).toInstance(cConf);
  }
}
