package com.continuuity.data2.dataset2.lib.table.proxy;

import com.continuuity.common.conf.Constants;
import com.continuuity.http.HttpHandler;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 *
 */
public class DatasetUserAdminHttpModule extends PrivateModule {

  @Override
  protected void configure() {
    Named name = Names.named(Constants.Service.DATASET_USER_ADMIN);
    Multibinder<HttpHandler> handlerBinder = Multibinder.newSetBinder(binder(), HttpHandler.class, name);
    handlerBinder.addBinding().to(DatasetUserAdminHttpHandler.class);

    bind(DatasetUserAdminService.class).in(Scopes.SINGLETON);
    expose(DatasetUserAdminService.class);
  }
}
