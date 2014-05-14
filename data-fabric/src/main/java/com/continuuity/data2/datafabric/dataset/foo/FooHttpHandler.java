package com.continuuity.data2.datafabric.dataset.foo;

import com.continuuity.http.AbstractHttpHandler;
import com.continuuity.http.HttpResponder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *
 */
@Path("/v1/foo")
public class FooHttpHandler extends AbstractHttpHandler {

  @GET
  @Path("/admin/{name}/exists")
  public void exists(HttpRequest request, final HttpResponder responder, @PathParam("name") String name) {
    responder.sendJson(HttpResponseStatus.OK, name);
  }

}
