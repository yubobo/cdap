package com.continuuity.data2.dataset2.user;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.datafabric.dataset.DataFabricDatasetManager;
import com.continuuity.gateway.auth.Authenticator;
import com.continuuity.gateway.handlers.AuthenticatedHttpHandler;
import com.continuuity.http.HttpResponder;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;
import java.util.Map;

/**
 * Provides REST endpoints for {@link DatasetAdmin} operations.
 */
@Path("/" + Constants.Dataset.User.VERSION)
public class DataSetAdminHTTPHandler extends AuthenticatedHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DataSetAdminHTTPHandler.class);

  private final DataFabricDatasetManager client;
  private final Map<String, AdminOp> adminOps;

  @Inject
  public DataSetAdminHTTPHandler(Authenticator authenticator, DataFabricDatasetManager client) {
    super(authenticator);
    this.client = client;
    this.adminOps = ImmutableMap.<String, AdminOp>builder()
      .put("exists", new AdminOp() {
        public AdminOpResponse apply(DatasetAdmin datasetAdmin) throws IOException {
          return new AdminOpResponse(datasetAdmin.exists(), null);
        }
      }).put("create", new AdminOp() {
        public AdminOpResponse apply(DatasetAdmin datasetAdmin) throws IOException {
          datasetAdmin.create();
          return new AdminOpResponse(null, null);
        }
      }).put("drop", new AdminOp() {
        public AdminOpResponse apply(DatasetAdmin datasetAdmin) throws IOException {
          datasetAdmin.drop();
          return new AdminOpResponse(null, null);
        }
      }).put("truncate", new AdminOp() {
        public AdminOpResponse apply(DatasetAdmin datasetAdmin) throws IOException {
          datasetAdmin.truncate();
          return new AdminOpResponse(null, null);
        }
      }).put("upgrade", new AdminOp() {
        public AdminOpResponse apply(DatasetAdmin datasetAdmin) throws IOException {
          datasetAdmin.upgrade();
          return new AdminOpResponse(null, null);
        }
      })
      .build();
  }

  @GET
  @Path("/datasets/{instance}/execute/{op}")
  public void executeOp(HttpRequest request, final HttpResponder responder,
                        @PathParam("instance") String instanceName, @PathParam("op") String opName) {

    AdminOp adminOp = adminOps.get(opName);
    if (adminOp == null) {
      throw new NotFoundException("AdminOp " + opName + " was invalid");
    }

    DatasetAdmin datasetAdmin = tryGetDatasetAdmin(instanceName);
    if (datasetAdmin == null) {
      throw new NotFoundException("DatasetAdmin was null for dataset instance" + instanceName);
    }

    try {
      AdminOpResponse response = adminOp.apply(datasetAdmin);
      responder.sendJson(HttpResponseStatus.OK, response);
    } catch (IOException e) {
      LOG.error("Error executing admin operation {} for dataset instance {}", e, opName, instanceName);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private DatasetAdmin tryGetDatasetAdmin(String instanceName) throws InternalServerErrorException {
    try {
      return client.getAdmin(instanceName, getClassLoader(instanceName));
    } catch (Exception e) {
      LOG.error("Error obtaining DatasetAdmin for instance " + instanceName, e);
      throw new InternalServerErrorException();
    }
  }

  private ClassLoader getClassLoader(String instanceName) {
    // TODO
    return null;
  }

  /**
   * TODO: improve this response.
   */
  private static final class AdminOpResponse {

    private Object result;
    private String message;

    public AdminOpResponse(Object result, String message) {
      this.result = result;
      this.message = message;
    }

    public Object getResult() {
      return result;
    }

    public String getMessage() {
      return message;
    }
  }

  private interface AdminOp {
    AdminOpResponse apply(DatasetAdmin datasetAdmin) throws IOException;
  }

}
