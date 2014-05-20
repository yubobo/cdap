package com.continuuity.data2.dataset2.user;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.datafabric.dataset.DataFabricDatasetManager;
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

/**
 * Provides REST endpoints for {@link DatasetAdmin} operations.
 */
@Path("/" + Constants.Dataset.User.VERSION)
public class DatasetUserAdminHandler extends AbstractHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUserAdminHandler.class);

  private final DataFabricDatasetManager client;

  @Inject
  public DatasetUserAdminHandler(/*DataFabricDatasetManager client*/) {
    this.client = null;
  }

  @GET
  @Path("/datasets/user/{name}/exists")
  public void exists(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    LOG.info("EXISTS! for {}", name);
    responder.sendJson(HttpResponseStatus.OK, "EXISTS(" + name + ")");
    /*
    try {
      DatasetAdmin admin = client.getAdmin(name, getClassLoader(request, name));
      boolean exists = admin.exists();
      responder.sendJson(HttpResponseStatus.OK, exists);
    } catch (Exception e) {
      LOG.error("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }*/
  }

  // TODO: other

  private ClassLoader getClassLoader(HttpRequest request, String name) {
    // TODO
    return null;
  }

}
