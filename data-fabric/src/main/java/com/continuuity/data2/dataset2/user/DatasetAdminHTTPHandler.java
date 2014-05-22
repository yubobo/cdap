package com.continuuity.data2.dataset2.user;

import com.continuuity.common.conf.Constants;
import com.continuuity.common.exception.HandlerException;
import com.continuuity.data2.datafabric.dataset.DataFabricDatasetManager;
import com.continuuity.data2.dataset2.manager.DatasetManagementException;
import com.continuuity.gateway.auth.Authenticator;
import com.continuuity.gateway.handlers.AuthenticatedHttpHandler;
import com.continuuity.http.HandlerContext;
import com.continuuity.http.HttpResponder;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Provides REST endpoints for {@link DatasetAdmin} operations.
 */
@Path(Constants.Gateway.GATEWAY_VERSION)
public class DataSetAdminHTTPHandler extends AuthenticatedHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DataSetAdminHTTPHandler.class);

  private final DataFabricDatasetManager client;

  @Inject
  public DataSetAdminHTTPHandler(Authenticator authenticator, DataFabricDatasetManager client) {
    super(authenticator);
    this.client = client;
  }

  @Override
  public void init(HandlerContext context) {
    super.init(context);
    client.startAndWait();
  }

  @Override
  public void destroy(HandlerContext context) {
    super.destroy(context);
    client.stopAndWait();
  }

  @GET
  @Path("/data/instances/{instance}/execute/exists")
  public void exists(HttpRequest request, HttpResponder responder, @PathParam("instance") String instanceName) {
    try {
      DatasetAdmin datasetAdmin = tryGetDatasetAdmin(instanceName);
      responder.sendJson(HttpResponseStatus.OK, new AdminOpResponse(datasetAdmin.exists(), null));
    } catch (HandlerException e) {
      LOG.info("Got handler exception", e);
      responder.sendError(e.getFailureStatus(), StringUtils.defaultIfEmpty(e.getMessage(), ""));
    } catch (Exception e) {
      LOG.error(getAdminOpErrorMessage("exists", instanceName), e);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, getAdminOpErrorMessage("exists", instanceName));
    }
  }

  @GET
  @Path("/data/instances/{instance}/execute/create")
  public void create(HttpRequest request, HttpResponder responder, @PathParam("instance") String instanceName) {
    try {
      DatasetAdmin datasetAdmin = tryGetDatasetAdmin(instanceName);
      datasetAdmin.create();
      responder.sendJson(HttpResponseStatus.OK, new AdminOpResponse(null, null));
    } catch (HandlerException e) {
      LOG.info("Got handler exception", e);
      responder.sendError(e.getFailureStatus(), StringUtils.defaultIfEmpty(e.getMessage(), ""));
    } catch (Exception e) {
      LOG.error(getAdminOpErrorMessage("create", instanceName), e);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, getAdminOpErrorMessage("create", instanceName));
    }
  }

  @GET
  @Path("/data/instances/{instance}/execute/drop")
  public void drop(HttpRequest request, HttpResponder responder, @PathParam("instance") String instanceName) {
    try {
      DatasetAdmin datasetAdmin = tryGetDatasetAdmin(instanceName);
      datasetAdmin.drop();
      responder.sendJson(HttpResponseStatus.OK, new AdminOpResponse(null, null));
    } catch (HandlerException e) {
      LOG.info("Got handler exception", e);
      responder.sendError(e.getFailureStatus(), StringUtils.defaultIfEmpty(e.getMessage(), ""));
    } catch (Exception e) {
      LOG.error(getAdminOpErrorMessage("drop", instanceName), e);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, getAdminOpErrorMessage("drop", instanceName));
    }
  }

  @GET
  @Path("/data/instances/{instance}/execute/truncate")
  public void truncate(HttpRequest request, HttpResponder responder, @PathParam("instance") String instanceName) {
    try {
      DatasetAdmin datasetAdmin = tryGetDatasetAdmin(instanceName);
      datasetAdmin.truncate();
      responder.sendJson(HttpResponseStatus.OK, new AdminOpResponse(null, null));
    } catch (HandlerException e) {
      LOG.info("Got handler exception", e);
      responder.sendError(e.getFailureStatus(), StringUtils.defaultIfEmpty(e.getMessage(), ""));
    } catch (Exception e) {
      LOG.error(getAdminOpErrorMessage("truncate", instanceName), e);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, getAdminOpErrorMessage("truncate", instanceName));
    }
  }

  @GET
  @Path("/data/instances/{instance}/execute/upgrade")
  public void upgrade(HttpRequest request, HttpResponder responder, @PathParam("instance") String instanceName) {
    try {
      DatasetAdmin datasetAdmin = tryGetDatasetAdmin(instanceName);
      datasetAdmin.upgrade();
      responder.sendJson(HttpResponseStatus.OK, new AdminOpResponse(null, null));
    } catch (HandlerException e) {
      LOG.info("Got handler exception", e);
      responder.sendError(e.getFailureStatus(), StringUtils.defaultIfEmpty(e.getMessage(), ""));
    } catch (Exception e) {
      LOG.error(getAdminOpErrorMessage("upgrade", instanceName), e);
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, getAdminOpErrorMessage("upgrade", instanceName));
    }
  }

  private String getAdminOpErrorMessage(String opName, String instanceName) {
    return String.format("Error executing admin operation %s for dataset instance %s", opName, instanceName);
  }

  private DatasetAdmin tryGetDatasetAdmin(String instanceName) throws IOException, DatasetManagementException {
    DatasetAdmin admin = client.getAdmin(instanceName, getClassLoader(instanceName));
    if (admin == null) {
      throw new HandlerException(HttpResponseStatus.NOT_FOUND,
                                 "Couldn't obtain DatasetAdmin for dataset instance " + instanceName);
    }
    return admin;
  }

  private ClassLoader getClassLoader(String instanceName) {
    // TODO
    return null;
  }

}
