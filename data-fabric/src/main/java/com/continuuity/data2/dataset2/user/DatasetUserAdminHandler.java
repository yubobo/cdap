package com.continuuity.data2.dataset2.user;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.datafabric.dataset.DataFabricDatasetManager;
import com.continuuity.data2.dataset2.manager.DatasetManagementException;
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
 * Provides REST endpoints for {@link DatasetAdmin} operations.
 */
@Path("/" + Constants.Dataset.User.VERSION)
public class DatasetUserAdminHandler extends AbstractHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUserAdminHandler.class);

  private final DataFabricDatasetManager client;

  @Inject
  public DatasetUserAdminHandler(DataFabricDatasetManager client) {
    this.client = client;
  }

  @GET
  @Path("/datasets/{instance}/execute/{op}")
  public void exists(HttpRequest request, final HttpResponder responder,
                     @PathParam("instance") String instanceName,
                     @PathParam("op") String opName) {

    AdminOpResponse response;

    try {
      DatasetAdmin admin = client.getAdmin(instanceName, getClassLoader(request, instanceName));
      boolean exists = admin.exists();
      response = new AdminOpResponse(exists, true, null);
    } catch (Exception e) {
      LOG.error("Error", e);
      response = new AdminOpResponse(null, false, "Error: " + e.getMessage());
    }

    responder.sendJson(response.success ? HttpResponseStatus.OK : HttpResponseStatus.INTERNAL_SERVER_ERROR, response);
  }

  // TODO: other

  private ClassLoader getClassLoader(HttpRequest request, String name) {
    // TODO
    return null;
  }

  /**
   * TODO: improve this response.
   */
  private static final class AdminOpResponse {
    private Object result;
    private boolean success;
    private String message;

    private AdminOpResponse(Object result, boolean success, String message) {
      this.result = result;
      this.success = success;
      this.message = message;
    }
  }

}
