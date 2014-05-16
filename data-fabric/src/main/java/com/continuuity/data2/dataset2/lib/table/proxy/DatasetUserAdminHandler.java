package com.continuuity.data2.dataset2.lib.table.proxy;

import com.continuuity.common.conf.Constants;
import com.continuuity.data2.dataset2.manager.DatasetManager;
import com.continuuity.http.HttpResponder;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.inject.Inject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Carries out {@link DatasetAdmin} operations.
 */
@Path(Constants.Dataset.User.VERSION + "/datasets/user/admin")
public class DatasetUserAdminHandler {

  private final DatasetManager client;

  @Inject
  public DatasetUserAdminHandler(DatasetManager client) {
    this.client = client;
  }

  @GET
  @Path("/{instanceName}/exists")
  public void exists(HttpRequest request, HttpResponder responder,
                      @PathParam("instanceName") String instanceName) throws Exception {
    DatasetAdmin admin = client.getAdmin(instanceName, getClassLoader(request, instanceName));
    boolean exists = admin != null && admin.exists();
    responder.sendJson(HttpResponseStatus.OK, exists);
  }

  // TODO: endpoints for other DatasetAdmin operations

  private ClassLoader getClassLoader(HttpRequest request, String instanceName) {
    // TODO
    return null;
  }

}
