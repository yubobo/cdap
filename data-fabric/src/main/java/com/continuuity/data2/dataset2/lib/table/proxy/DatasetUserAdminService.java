package com.continuuity.data2.dataset2.lib.table.proxy;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.hooks.MetricsReporterHook;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.http.HttpHandler;
import com.continuuity.http.NettyHttpService;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.twill.common.Cancellable;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryService;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 *
 */
public class DatasetUserAdminService extends AbstractIdleService {

  private final DiscoveryService discoveryService;
  private final NettyHttpService httpService;
  private Cancellable cancellable;

  @Inject
  public DatasetUserAdminService(CConfiguration cConf, DiscoveryService discoveryService,
                           @Named(Constants.Service.DATASET_USER_ADMIN) Set<HttpHandler> handlers,
                           @Nullable MetricsCollectionService metricsCollectionService) {

    this.discoveryService = discoveryService;

    int workerThreads = cConf.getInt(Constants.Stream.WORKER_THREADS, 10);
    this.httpService = NettyHttpService.builder()
      .addHttpHandlers(handlers)
      .setHandlerHooks(ImmutableList.of(new MetricsReporterHook(metricsCollectionService, Constants.Service.DATASET_USER_ADMIN)))
      .setHost(cConf.get(Constants.Dataset.UserAdmin.ADDRESS))
      .setWorkerThreadPoolSize(workerThreads)
      .setExecThreadPoolSize(0)
      .setConnectionBacklog(20000)
      .build();
  }

  @Override
  protected void startUp() throws Exception {
    httpService.startAndWait();
    cancellable = discoveryService.register(new Discoverable() {
      @Override
      public String getName() {
        return Constants.Service.DATASET_USER_ADMIN;
      }

      @Override
      public InetSocketAddress getSocketAddress() {
        return httpService.getBindAddress();
      }
    });
  }

  @Override
  protected void shutDown() throws Exception {
    try {
      if (cancellable != null) {
        cancellable.cancel();
      }
    } finally {
      httpService.stopAndWait();
    }
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("bindAddress", httpService.getBindAddress())
      .toString();
  }
}
