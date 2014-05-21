package com.continuuity.data2.dataset2.user;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.dataset2.manager.inmemory.DefaultDatasetDefinitionRegistry;
import com.continuuity.data2.dataset2.manager.inmemory.InMemoryDatasetDefinitionRegistry;
import com.continuuity.http.HttpHandler;
import com.continuuity.internal.data.dataset.module.DatasetDefinitionRegistry;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Module containing {@link DatasetUserService}.
 */
public class DatasetUserHttpModule extends PrivateModule {

  @Override
  protected void configure() {
    Named name = Names.named(Constants.Service.DATASET_USER);
    Multibinder<HttpHandler> handlerBinder = Multibinder.newSetBinder(binder(), HttpHandler.class, name);
    handlerBinder.addBinding().to(DatasetUserAdminHandler.class);

    bind(DatasetDefinitionRegistry.class).to(DefaultDatasetDefinitionRegistry.class);
    bind(DatasetUserService.class).in(Scopes.SINGLETON);
    expose(DatasetUserService.class);
  }
}
