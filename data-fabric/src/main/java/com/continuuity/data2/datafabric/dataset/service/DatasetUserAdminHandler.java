package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.Constants;
import com.continuuity.http.AbstractHttpHandler;
import com.continuuity.http.HttpResponder;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.common.net.HostAndPort;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;

/**
 * Handles dataset administrative calls by forwarding calls to a particular
 * {@link com.continuuity.data2.datafabric.dataset.service.DatasetAdminHandler},
 * which is running its own Twill application. This separation is done to make
 * {@link com.continuuity.data2.datafabric.dataset.service.DatasetAdminHandler} operations
 * run by a particular user, which allows us to secure arbitrary code written by
 * user-defined datasets.
 */
@Path("/" + Constants.Dataset.Manager.VERSION)
public class DatasetUserAdminHandler extends AbstractHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUserAdminHandler.class);

  /**
   * User to execute administrative commands as.
   */
  private final String user;
  private final HostAndPort adminHandlerHostAndPort;

  public DatasetUserAdminHandler(String user, HostAndPort adminHandlerHostAndPort) {
    this.user = user;
    this.adminHandlerHostAndPort = adminHandlerHostAndPort;
  }

  @GET
  @Path("/datasets/admin/{name}/exists")
  public void exists(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    String url = String.format("http://%s/datasets/admin/%s", adminHandlerHostAndPort.toString(), name);
    applyDatasetAdminOperation(responder, url);
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
    String url = String.format("http://%s/datasets/admin/drop/%s", adminHandlerHostAndPort.toString(), name);
    applyDatasetAdminOperation(responder, url);
  }

  @GET
  @Path("/datasets/admin/{name}/drop")
  public void truncate(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    String url = String.format("http://%s/datasets/admin/truncate/%s", adminHandlerHostAndPort.toString(), name);
    applyDatasetAdminOperation(responder, url);
  }

  @GET
  @Path("/datasets/admin/{name}/upgrade")
  public void upgrade(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    String url = String.format("http://%s/datasets/admin/upgrade/%s", adminHandlerHostAndPort.toString(), name);
    applyDatasetAdminOperation(responder, url);
  }

  private void applyDatasetAdminOperation(final HttpResponder responder, String url) {
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet(url);
    try {
      HttpResponse response = httpClient.execute(httpGet);
      responder.sendJson(HttpResponseStatus.OK, response.getEntity().getContent());
    } catch (IOException e) {
      LOG.error("Error", e);
      responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
