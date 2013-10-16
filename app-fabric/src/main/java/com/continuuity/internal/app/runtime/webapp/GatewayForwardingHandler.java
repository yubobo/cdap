package com.continuuity.internal.app.runtime.webapp;

import com.continuuity.common.conf.Constants;
import com.continuuity.common.discovery.EndpointStrategy;
import com.continuuity.common.discovery.RandomEndpointStrategy;
import com.continuuity.common.discovery.TimeLimitEndpointStrategy;
import com.continuuity.common.http.core.AbstractHttpHandler;
import com.continuuity.common.http.core.HandlerContext;
import com.continuuity.common.http.core.HttpResponder;
import com.continuuity.common.http.forwarder.RequestForwarder;
import com.continuuity.weave.discovery.Discoverable;
import com.continuuity.weave.discovery.DiscoveryServiceClient;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * Forwards calls to Gateway.
 */
public class GatewayForwardingHandler extends AbstractHttpHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GatewayForwardingHandler.class);
  private static final long DISCOVERY_TIMEOUT_SECONDS = 1L;

  private final DiscoveryServiceClient discoveryServiceClient;
  private final RequestForwarder forwarder = new RequestForwarder();

  private EndpointStrategy discoverableStrategy;

  public GatewayForwardingHandler(DiscoveryServiceClient discoveryServiceClient) {
    this.discoveryServiceClient = discoveryServiceClient;
  }

  @Override
  public void init(HandlerContext context) {
    LOG.info("Starting GatewayForwardingHandler for service {}", Constants.Service.GATEWAY);
    discoverableStrategy = new TimeLimitEndpointStrategy(
      new RandomEndpointStrategy(discoveryServiceClient.discover(Constants.Service.GATEWAY)),
      DISCOVERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
  }

  @Override
  public void destroy(HandlerContext context) {
    forwarder.close();
    LOG.info("Stopping GatewayForwardingHandler...");
  }

  @GET
  @Path("/v2/.*")
  public void forwardGet(HttpRequest request, HttpResponder responder) {
    Discoverable discoverable = discoverableStrategy.pick();
    if (discoverable == null) {
      LOG.warn("No endpoint for service {}", Constants.Service.GATEWAY);
      responder.sendStatus(NOT_FOUND);
      return;
    }

    InetSocketAddress endpoint = discoverable.getSocketAddress();
    request.setUri(String.format("http://%s:%d%s", endpoint.getHostName(), endpoint.getPort(), request.getUri()));
    forwarder.forward(request, responder);
  }

  @PUT
  @Path("/v2/.*")
  public void forwardPut(HttpRequest request, HttpResponder responder) {
    Discoverable discoverable = discoverableStrategy.pick();
    if (discoverable == null) {
      LOG.warn("No endpoint for service {}", Constants.Service.GATEWAY);
      responder.sendStatus(NOT_FOUND);
      return;
    }

    InetSocketAddress endpoint = discoverable.getSocketAddress();
    request.setUri(String.format("http://%s:%d%s", endpoint.getHostName(), endpoint.getPort(), request.getUri()));
    forwarder.forward(request, responder);
  }

  @POST
  @Path("/v2/.*")
  public void forwardPost(HttpRequest request, HttpResponder responder) {
    Discoverable discoverable = discoverableStrategy.pick();
    if (discoverable == null) {
      LOG.warn("No endpoint for service {}", Constants.Service.GATEWAY);
      responder.sendStatus(NOT_FOUND);
      return;
    }

    InetSocketAddress endpoint = discoverable.getSocketAddress();
    request.setUri(String.format("http://%s:%d%s", endpoint.getHostName(), endpoint.getPort(), request.getUri()));
    forwarder.forward(request, responder);
  }

  @DELETE
  @Path("/v2/.*")
  public void forwardDelete(HttpRequest request, HttpResponder responder) {
    Discoverable discoverable = discoverableStrategy.pick();
    if (discoverable == null) {
      LOG.warn("No endpoint for service {}", Constants.Service.GATEWAY);
      responder.sendStatus(NOT_FOUND);
      return;
    }

    InetSocketAddress endpoint = discoverable.getSocketAddress();
    request.setUri(String.format("http://%s:%d%s", endpoint.getHostName(), endpoint.getPort(), request.getUri()));
    forwarder.forward(request, responder);
  }

}
