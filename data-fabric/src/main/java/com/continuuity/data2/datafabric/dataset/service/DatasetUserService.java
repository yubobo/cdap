package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data2.datafabric.dataset.DataFabricDatasetManager;
import com.continuuity.data2.datafabric.dataset.client.DatasetManagerServiceClient;
import com.continuuity.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.twill.api.TwillRunner;
import org.apache.twill.api.TwillRunnerService;
import org.apache.twill.common.Cancellable;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryService;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.apache.twill.filesystem.LocationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Handles various dataset operations running as a particular user.
 */
public class DatasetUserService extends AbstractIdleService {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUserService.class);

  private final NettyHttpService httpService;
  private final String user;
  private final DataFabricDatasetManager dsService;
  private final DiscoveryService discoveryService;

  private Cancellable cancelDiscovery;

  @Inject
  public DatasetUserService(CConfiguration cConf,
                               LocationFactory locationFactory,
                               @Named(Constants.Dataset.UserService.ADDRESS) InetAddress hostname,
                               DiscoveryService discoveryService,
                               TwillRunner twillRunner,
                               DiscoveryServiceClient discoveryServiceClient,
                               DataFabricDatasetManager dsService
  ) throws Exception {

    NettyHttpService.Builder builder = NettyHttpService.builder();
    // TODO: pass proper user
    this.user = "bob";
    this.dsService = dsService;
    this.discoveryService = discoveryService;

    // TODO: use random port, since we want to run one DatasetUserService per reactor user.
    builder.addHttpHandlers(ImmutableList.of(
      new DatasetUserAdminHandler(user, twillRunner, discoveryServiceClient, dsService)));

    builder.setHost(hostname.getCanonicalHostName());

    builder.setPort(cConf.getInt(Constants.Dataset.UserService.PORT, Constants.Dataset.UserService.DEFAULT_PORT));

    builder.setConnectionBacklog(cConf.getInt(Constants.Dataset.UserService.BACKLOG_CONNECTIONS,
                                              Constants.Dataset.UserService.DEFAULT_BACKLOG));
    builder.setExecThreadPoolSize(cConf.getInt(Constants.Dataset.UserService.EXEC_THREADS,
                                               Constants.Dataset.UserService.DEFAULT_EXEC_THREADS));
    builder.setBossThreadPoolSize(cConf.getInt(Constants.Dataset.UserService.BOSS_THREADS,
                                               Constants.Dataset.UserService.DEFAULT_BOSS_THREADS));
    builder.setWorkerThreadPoolSize(cConf.getInt(Constants.Dataset.UserService.WORKER_THREADS,
                                                 Constants.Dataset.UserService.DEFAULT_WORKER_THREADS));

    this.httpService = builder.build();
  }

  @Override
  protected void startUp() throws Exception {
    LOG.info("Starting DatasetUserService for user {}...", user);

    httpService.startAndWait();

    // Register the service
    cancelDiscovery = discoveryService.register(new Discoverable() {
      @Override
      public String getName() {
        // TODO(alvin): some function to get the service name of a user service by user name
        return Constants.Service.DATASET_USER + "." + user;
      }

      @Override
      public InetSocketAddress getSocketAddress() {
        return httpService.getBindAddress();
      }
    });

    LOG.info("DatasetManagerService started successfully on {}", httpService.getBindAddress());
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.info("Stopping DatasetUserService...");

    // Unregister the service
    cancelDiscovery.cancel();
    // Wait for a few seconds for requests to stop
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      LOG.error("Interrupted while waiting...", e);
    }

    httpService.stopAndWait();
  }
}
