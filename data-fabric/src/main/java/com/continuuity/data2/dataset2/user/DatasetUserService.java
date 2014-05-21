package com.continuuity.data2.dataset2.user;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.http.HttpHandler;
import com.continuuity.http.NettyHttpService;
import com.google.common.base.Objects;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.twill.common.Cancellable;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * Provides various REST endpoints to execute user code via {@link DatasetUserAdminHandler}.
 */
public class DatasetUserService extends AbstractIdleService {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUserService.class);

  private final DiscoveryService discoveryService;
  private final NettyHttpService httpService;
  private Cancellable cancellable;

  @Inject
  public DatasetUserService(CConfiguration cConf, DiscoveryService discoveryService,
                            @Named(Constants.Service.DATASET_USER) Set<HttpHandler> handlers) {

    this.discoveryService = discoveryService;

    int workerThreads = cConf.getInt(Constants.Dataset.User.WORKER_THREADS, 10);
    this.httpService = NettyHttpService.builder()
      .addHttpHandlers(handlers)
      .setHost(cConf.get(Constants.Dataset.User.ADDRESS))
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
        return Constants.Service.DATASET_USER;
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
