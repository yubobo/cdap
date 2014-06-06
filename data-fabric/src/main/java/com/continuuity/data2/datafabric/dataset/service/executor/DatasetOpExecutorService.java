package com.continuuity.data2.datafabric.dataset.service.executor;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.hooks.MetricsReporterHook;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.http.HttpHandler;
import com.continuuity.http.NettyHttpService;
import com.continuuity.security.auth.AuthenticationChannelHandler;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.twill.common.Cancellable;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryService;
import org.jboss.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Provides various REST endpoints to execute user code via {@link DatasetAdminOpHTTPHandler}.
 */
public class DatasetOpExecutorService extends AbstractIdleService {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetOpExecutorService.class);

  private final DiscoveryService discoveryService;
  private final NettyHttpService httpService;
  private Cancellable cancellable;
  private final CConfiguration configuration;
  private final AuthenticationChannelHandler authenticationChannelHandler;

  @Inject
  public DatasetOpExecutorService(CConfiguration cConf, DiscoveryService discoveryService,
                                  MetricsCollectionService metricsCollectionService,
                                  @Named(Constants.Service.DATASET_EXECUTOR) Set<HttpHandler> handlers,
                                  AuthenticationChannelHandler authenticationChannelHandler) {

    this.discoveryService = discoveryService;
    this.configuration = cConf;
    this.authenticationChannelHandler = authenticationChannelHandler;

    int workerThreads = cConf.getInt(Constants.Dataset.Executor.WORKER_THREADS, 10);
    int execThreads = cConf.getInt(Constants.Dataset.Executor.EXEC_THREADS, 10);

    this.httpService = NettyHttpService.builder()
      .addHttpHandlers(handlers)
      .setHost(cConf.get(Constants.Dataset.Executor.ADDRESS))
      .setHandlerHooks(ImmutableList.of(
        new MetricsReporterHook(metricsCollectionService, Constants.Service.DATASET_EXECUTOR)))
      .setWorkerThreadPoolSize(workerThreads)
      .setExecThreadPoolSize(execThreads)
      .setConnectionBacklog(20000)
      .modifyChannelPipeline(getChannelModifier())
      .build();
  }

  private Function<ChannelPipeline, ChannelPipeline> getChannelModifier() {
    if (configuration.getBoolean(Constants.Security.CFG_SECURITY_ENABLED)) {
      return new Function<ChannelPipeline, ChannelPipeline>() {
        @Nullable
        @Override
        public ChannelPipeline apply(@Nullable ChannelPipeline input) {
          input.addAfter("decoder", AuthenticationChannelHandler.HANDLER_NAME, authenticationChannelHandler);
          return input;
        }
      };
    } else {
      return null;
    }
  }

  @Override
  protected void startUp() throws Exception {
    LOG.info("Starting DatasetOpExecutorService...");

    httpService.startAndWait();
    cancellable = discoveryService.register(new Discoverable() {
      @Override
      public String getName() {
        return Constants.Service.DATASET_EXECUTOR;
      }

      @Override
      public InetSocketAddress getSocketAddress() {
        return httpService.getBindAddress();
      }
    });

    LOG.info("DatasetOpExecutorService started successfully on {}", httpService.getBindAddress());
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.info("Stopping DatasetOpExecutorService...");

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

  public NettyHttpService getHttpService() {
    return httpService;
  }
}
