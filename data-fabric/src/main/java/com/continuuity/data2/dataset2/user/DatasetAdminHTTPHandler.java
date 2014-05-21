package com.continuuity.data2.dataset2.user;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.datafabric.dataset.DataFabricDatasetManager;
import com.continuuity.data2.dataset2.user.admin.AdminOp;
import com.continuuity.data2.dataset2.user.admin.AdminOpResponse;
import com.continuuity.data2.dataset2.user.admin.AdminOps;
import com.continuuity.gateway.auth.Authenticator;
import com.continuuity.gateway.handlers.AuthenticatedHttpHandler;
import com.continuuity.http.HttpResponder;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.inject.Inject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Provides REST endpoints for {@link DatasetAdmin} operations.
 */
@Path("/" + Constants.Dataset.User.VERSION)
public class DatasetAdminHTTPHandler extends AuthenticatedHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetAdminHTTPHandler.class);

  private final DataFabricDatasetManager client;
  private final Map<String, AdminOp> adminOps;

  @Inject
  public DatasetAdminHTTPHandler(Authenticator authenticator, DataFabricDatasetManager client) {
    super(authenticator);
    this.client = client;
    this.adminOps = AdminOps.getDefaultAdminOps();
  }

  @GET
  @Path("/datasets/{instance}/execute/{op}")
  public void executeOp(HttpRequest request, final HttpResponder responder,
                        @PathParam("instance") String instanceName, @PathParam("op") String opName) {

    AdminOpResponse response;
    AdminOp adminOp = adminOps.get(opName);

    if (adminOp == null) {
      response = new AdminOpResponse(null, false, "Invalid operation: " + opName);
      responder.sendJson(HttpResponseStatus.NOT_FOUND, response);
      return;
    }

    try {
      ClassLoader classLoader = getClassLoader(instanceName);
      response = adminOp.execute(client, instanceName, classLoader);
    } catch (Exception e) {
      LOG.error("Error", e);
      response = new AdminOpResponse(null, false, "Error: " + e.getMessage());
    }

    HttpResponseStatus status = response.isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.INTERNAL_SERVER_ERROR;
    responder.sendJson(status, response);
  }

  private ClassLoader getClassLoader(String instanceName) {
    // TODO
    return null;
  }

}
