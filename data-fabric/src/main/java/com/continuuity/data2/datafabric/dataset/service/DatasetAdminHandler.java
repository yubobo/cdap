package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.datafabric.dataset.client.DatasetUserServiceClient;
import com.continuuity.data2.datafabric.dataset.runtime.DatasetUserRunnable;
import com.continuuity.data2.dataset2.manager.DatasetManager;
import com.continuuity.http.AbstractHttpHandler;
import com.continuuity.http.HttpResponder;
import com.google.inject.Inject;
import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillRunnerService;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles dataset administrative calls by forwarding calls from a particular user to a
 * {@link DatasetUserAdminHandler} that belongs to the particular user.
 */
@Path("/" + Constants.Dataset.UserService.VERSION)
public class DatasetAdminHandler extends AbstractHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetAdminHandler.class);

  private final DatasetManager datasetManager;
  // TODO(alvin): probably move this "up" into DatasetManager so that we can also forward non-admin tasks
  private final Map<String, DatasetUserServiceClient> userServices;
  private final TwillRunnerService runnerService;
  private final DiscoveryServiceClient discoveryServiceClient;

  @Inject
  public DatasetAdminHandler(DatasetManager datasetManager, TwillRunnerService runnerService,
                             DiscoveryServiceClient discoveryServiceClient) {
    this.datasetManager = datasetManager;
    this.runnerService = runnerService;
    this.discoveryServiceClient = discoveryServiceClient;
    this.userServices = new HashMap<String, DatasetUserServiceClient>();
  }

  @GET
  @Path("/datasets/admin/{name}/exists")
  public void exists(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    DatasetUserServiceClient dsUserClient = getOrCreateUserService(getUser(request));

    try {
      boolean exists = dsUserClient.exists(name);
      responder.sendJson(HttpResponseStatus.OK, exists);
    } catch (IOException e) {
      LOG.error("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/datasets/admin/{name}/create")
  public void create(HttpRequest request, final HttpResponder responder) {
    // TODO
    responder.sendStatus(HttpResponseStatus.NOT_IMPLEMENTED);
  }

  @GET
  @Path("/datasets/admin/{name}/drop")
  public void drop(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    responder.sendStatus(HttpResponseStatus.NOT_IMPLEMENTED);
  }

  @GET
  @Path("/datasets/admin/{name}/truncate")
  public void truncate(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    responder.sendStatus(HttpResponseStatus.NOT_IMPLEMENTED);
  }

  @GET
  @Path("/datasets/admin/{name}/upgrade")
  public void upgrade(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    responder.sendStatus(HttpResponseStatus.NOT_IMPLEMENTED);
  }

  private DatasetUserServiceClient getOrCreateUserService(String user) {
    DatasetUserServiceClient userServiceClient = userServices.get(user);
    if (userServiceClient != null) {
      return userServiceClient;
    }

    String runnableName = "dataset.user." + user;
    LOG.info("Starting dataset user service {}", runnableName);
    DatasetUserRunnable datasetUserRunnable = new DatasetUserRunnable(runnableName, null, null);
    TwillController twillController = runnerService.prepare(datasetUserRunnable).start();
    DatasetUserServiceClient newUserServiceClient = new DatasetUserServiceClient(user, discoveryServiceClient);
    return newUserServiceClient;
  }

  // TODO(alvin): implement
  private String getUser(HttpRequest request) {
    return "bob";
  }

}
