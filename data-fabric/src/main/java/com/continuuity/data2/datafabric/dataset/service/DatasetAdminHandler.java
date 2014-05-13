package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.dataset2.manager.DatasetManagementException;
import com.continuuity.data2.dataset2.manager.DatasetManager;
import com.continuuity.http.AbstractHttpHandler;
import com.continuuity.http.HttpResponder;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;

/**
 * Handles dataset administrative calls.
 */
@Path("/" + Constants.Dataset.UserService.VERSION)
public class DatasetAdminHandler extends AbstractHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetAdminHandler.class);

  private final DatasetManager datasetManager;
  private final ClassLoader classLoader;
  /**
   * User to execute administrative commands as.
   */
  private final String user;

  public DatasetAdminHandler(String user, DatasetManager datasetManager, ClassLoader classLoader) {
    this.user = user;
    this.datasetManager = datasetManager;
    this.classLoader = classLoader;
  }

  @GET
  @Path("/datasets/admin/{name}/exists")
  public void exists(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    try {
      DatasetAdmin datasetAdmin = datasetManager.getAdmin(name, classLoader);
      if (datasetAdmin != null) {
        boolean exists = datasetAdmin.exists();
        responder.sendJson(HttpResponseStatus.OK, exists);
      }
    } catch (DatasetManagementException e) {
      LOG.info("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    } catch (IOException e) {
      LOG.info("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/datasets/admin/create")
  public void create(HttpRequest request, final HttpResponder responder) {
    // TODO
    responder.sendStatus(HttpResponseStatus.NOT_FOUND);
  }

  @GET
  @Path("/datasets/admin/{name}/drop")
  public void drop(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    applyDatasetAdminOperation(responder, name, new DatasetAdminOperation<String>() {
      @Override
      public String apply(DatasetAdmin datasetAdmin) throws IOException {
        datasetAdmin.drop();
        return "";
      }
    });
  }

  @GET
  @Path("/datasets/admin/{name}/truncate")
  public void truncate(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    applyDatasetAdminOperation(responder, name, new DatasetAdminOperation<String>() {
      @Override
      public String apply(DatasetAdmin datasetAdmin) throws IOException {
        datasetAdmin.truncate();
        return "";
      }
    });
  }

  @GET
  @Path("/datasets/admin/{name}/upgrade")
  public void upgrade(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    applyDatasetAdminOperation(responder, name, new DatasetAdminOperation<String>() {
      @Override
      public String apply(DatasetAdmin datasetAdmin) throws IOException {
        datasetAdmin.upgrade();
        return "";
      }
    });
  }

  private void applyDatasetAdminOperation(final HttpResponder responder, String name,
                                          DatasetAdminOperation<?> datasetAdminOperation) {
    try {
      DatasetAdmin datasetAdmin = datasetManager.getAdmin(name, classLoader);
      if (datasetAdmin != null) {
        Object result = datasetAdminOperation.apply(datasetAdmin);
        if (result != null && !(result instanceof Void)) {
          responder.sendJson(HttpResponseStatus.OK, result);
        } else {
          responder.sendStatus(HttpResponseStatus.OK);
        }
      } else {
        responder.sendStatus(HttpResponseStatus.NOT_FOUND);
      }
    } catch (DatasetManagementException e) {
      LOG.info("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    } catch (IOException e) {
      LOG.info("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public interface DatasetAdminOperation<T> {
    T apply(DatasetAdmin datasetAdmin) throws IOException;
  }

}
