package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.Constants;
import com.continuuity.http.HttpHandler;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class DatasetUserHttpModule extends PrivateModule {

  @Override
  protected void configure() {
    Multibinder<HttpHandler> handlerBinder = Multibinder.newSetBinder(binder(), HttpHandler.class,
                                                                      Names.named(Constants.Service.DATASET_USER));
    handlerBinder.addBinding().to(DatasetUserAdminHandler.class);

    bind(DatasetUserService.class).in(Scopes.SINGLETON);
    expose(DatasetUserService.class);
  }
}