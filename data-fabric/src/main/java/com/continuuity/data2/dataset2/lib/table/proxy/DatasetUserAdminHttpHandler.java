package com.continuuity.data2.dataset2.lib.table.proxy;

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
 *
 */
@Path("/" + Constants.Dataset.Manager.VERSION)
public class DatasetUserAdminHttpHandler extends AbstractHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUserAdminHttpHandler.class);

  private final DataFabricDatasetManager client;

  @Inject
  public DatasetUserAdminHttpHandler(DataFabricDatasetManager client) {
    this.client = client;
  }

  @GET
  @Path("/datasets/user/{name}/exists")
  public void exists(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    try {
      DatasetAdmin admin = client.getAdmin(name, getClassLoader(request, name));
      boolean exists = admin.exists();
      responder.sendJson(HttpResponseStatus.OK, exists);
    } catch (Exception e) {
      LOG.error("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private ClassLoader getClassLoader(HttpRequest request, String name) {
    // TODO
    return null;
  }

}
