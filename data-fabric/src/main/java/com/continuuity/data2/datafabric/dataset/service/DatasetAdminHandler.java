package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.dataset2.manager.DatasetManagementException;
import com.continuuity.data2.dataset2.manager.DatasetManager;
import com.continuuity.http.AbstractHttpHandler;
import com.continuuity.http.HttpResponder;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.inject.Inject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;

/**
 * Handles dataset administrative calls by forwarding calls from a particular user to a
 * {@link DatasetUserAdminHandler} that belongs to the particular user.
 */
@Path("/" + Constants.Dataset.UserService.VERSION)
public class DatasetAdminHandler extends AbstractHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetAdminHandler.class);

  private final DatasetManager datasetManager;

  @Inject
  public DatasetAdminHandler(DatasetManager datasetManager) {
    this.datasetManager = datasetManager;
  }

  @GET
  @Path("/datasets/admin/{name}/exists")
  public void exists(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    responder.sendStatus(HttpResponseStatus.NOT_IMPLEMENTED);
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

  // TODO(alvin): implement
  private String getUser(HttpRequest request) {
    return "bob";
  }

}
