package com.continuuity.data2.datafabric.dataset.service;

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
 * Handles dataset administration calls for a particular user.
 */
@Path("/" + Constants.Dataset.Manager.VERSION)
public class DatasetUserAdminHandler extends AbstractHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUserAdminHandler.class);

  /**
   * User to execute administration commands as.
   */
  private final String user;
  private final DataFabricDatasetManager dsService;

  @Inject
  public DatasetUserAdminHandler(String user, DataFabricDatasetManager dsService) {
    this.user = user;
    this.dsService = dsService;
  }

  @GET
  @Path("/datasets/admin/{name}/exists")
  public void exists(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    applyDatasetAdminOperation(responder, name, new DatasetAdminOperation() {
      @Override
      public JsonResponse apply(DatasetAdmin datasetAdmin) throws IOException {
        boolean exists = datasetAdmin != null && datasetAdmin.exists();
        return new JsonResponse(HttpResponseStatus.OK, exists);
      }
    });
  }

  @GET
  @Path("/datasets/admin/{name}/create")
  public void create(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    applyDatasetAdminOperation(responder, name, new DatasetAdminOperation() {
      @Override
      public JsonResponse apply(DatasetAdmin datasetAdmin) throws IOException {
        // TODO
        // datasetAdmin.create();
        return new JsonResponse(HttpResponseStatus.NOT_IMPLEMENTED, null);
      }
    });
  }

  @GET
  @Path("/datasets/admin/{name}/drop")
  public void drop(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    applyDatasetAdminOperation(responder, name, new DatasetAdminOperation() {
      @Override
      public JsonResponse apply(DatasetAdmin datasetAdmin) throws IOException {
        datasetAdmin.drop();
        return new JsonResponse(HttpResponseStatus.OK, null);
      }
    });
  }

  @GET
  @Path("/datasets/admin/{name}/drop")
  public void truncate(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    applyDatasetAdminOperation(responder, name, new DatasetAdminOperation() {
      @Override
      public JsonResponse apply(DatasetAdmin datasetAdmin) throws IOException {
          datasetAdmin.truncate();
          return new JsonResponse(HttpResponseStatus.OK, null);
      }
    });
  }

  @GET
  @Path("/datasets/admin/{name}/upgrade")
  public void upgrade(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    applyDatasetAdminOperation(responder, name, new DatasetAdminOperation() {
      @Override
      public JsonResponse apply(DatasetAdmin datasetAdmin) throws IOException {
        datasetAdmin.upgrade();
        return new JsonResponse(HttpResponseStatus.OK, null);
      }
    });
  }

  private DatasetAdmin getDatasetAdmin(String instanceName) throws IOException, DatasetManagementException {
    return dsService.getAdmin(instanceName, getClassLoader(instanceName));
  }

  private ClassLoader getClassLoader(String instanceName) {
    // TODO(alvin): implement
    return null;
  }

  private void applyDatasetAdminOperation(final HttpResponder responder, String instanceName,
                                          DatasetAdminOperation operation) {

    try {
      DatasetAdmin datasetAdmin = getDatasetAdmin(instanceName);
      JsonResponse result = operation.apply(datasetAdmin);
      responder.sendJson(result.getStatusCode(), result.object);
    } catch (Exception e) {
      LOG.error("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private interface DatasetAdminOperation {
    public JsonResponse apply(DatasetAdmin datasetAdmin) throws IOException;
  }

  private static final class JsonResponse<T> {
    private final HttpResponseStatus statusCode;
    private final T object;

    private JsonResponse(HttpResponseStatus statusCode, T object) {
      this.statusCode = statusCode;
      this.object = object;
    }

    public HttpResponseStatus getStatusCode() {
      return statusCode;
    }

    public T getObject() {
      return object;
    }
  }

}
