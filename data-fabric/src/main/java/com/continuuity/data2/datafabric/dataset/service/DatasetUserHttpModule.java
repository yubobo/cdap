package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.dataset2.manager.inmemory.InMemoryDatasetDefinitionRegistry;
import com.continuuity.http.HttpHandler;
import com.continuuity.internal.data.dataset.module.DatasetDefinitionRegistry;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 */
public class DatasetUserHttpModule extends PrivateModule {

  @Override
  protected void configure() {
    Multibinder<HttpHandler> handlerBinder = Multibinder.newSetBinder(binder(), HttpHandler.class,
                                                                      Names.named(Constants.Service.DATASET_USER));
    handlerBinder.addBinding().to(DatasetUserAdminHandler.class);

    bind(InetAddress.class)
      .annotatedWith(Names.named(Constants.Dataset.UserService.ADDRESS))
      .toInstance(getAddress());

    // TODO(alvin): move this
    bind(DatasetDefinitionRegistry.class).to(InMemoryDatasetDefinitionRegistry.class);
    bind(DatasetUserService.class).in(Scopes.SINGLETON);
    expose(DatasetUserService.class);
  }

  private InetAddress getAddress() {
    try {
      return InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      return null;
    }
  }
}