package com.continuuity.common.http.forwarder;

import com.continuuity.common.http.core.AbstractHttpHandler;
import com.continuuity.common.http.core.HandlerContext;
import com.continuuity.common.http.core.HttpResponder;
import com.continuuity.common.http.core.NettyHttpService;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.util.concurrent.Service;
import com.ning.http.client.AsyncHttpClientConfig;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test RequestForwarder.
 */
public class RequestForwarderTest {
  private static String hostname = "127.0.0.1";

  private static int servicePort;
  private static NettyHttpService service;

  private static int destServicePort;
  private static NettyHttpService destService;

  @BeforeClass
  public static void start() throws Exception {
    NettyHttpService.Builder serviceBuilder = NettyHttpService.builder();
    serviceBuilder.addHttpHandlers(ImmutableList.of(new ServiceHandler()));
    serviceBuilder.setHost(hostname);

    service = serviceBuilder.build();
    service.startAndWait();
    Service.State state = service.state();
    assertEquals(Service.State.RUNNING, state);
    servicePort = service.getBindAddress().getPort();

    NettyHttpService.Builder destServiceBuilder = NettyHttpService.builder();
    destServiceBuilder.addHttpHandlers(ImmutableList.of(new DestServiceHandler()));
    destServiceBuilder.setHost(hostname);

    destService = destServiceBuilder.build();
    destService.startAndWait();
    state = destService.state();
    assertEquals(Service.State.RUNNING, state);
    destServicePort = destService.getBindAddress().getPort();
  }

  @AfterClass
  public static void stop() {
    service.startAndWait();
    destService.startAndWait();
  }

  @Test
  public void testGet() throws Exception {
    String param1 = "value1";
    String header = "get-header";

    DefaultHttpClient client = new DefaultHttpClient();
    HttpGet get = new HttpGet(String.format("http://%s:%d/dest/serve?param1=%s", hostname, servicePort, param1));
    get.setHeader("X-Test-Header", header);
    HttpResponse response = client.execute(get);

    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    Assert.assertEquals(param1, EntityUtils.toString(response.getEntity()));
    Assert.assertNotNull(response.getHeaders("X-Test-Header"));
    Assert.assertEquals(header, response.getHeaders("X-Test-Header")[0].getValue());
  }

  @Test
  public void testPut() throws Exception {
    String content = "put-content";
    String header = "put-header";

    DefaultHttpClient client = new DefaultHttpClient();
    HttpPut put = new HttpPut(String.format("http://%s:%d/dest/serve/id1", hostname, servicePort));
    put.setHeader("X-Test-Header", header);
    put.setEntity(new StringEntity(content));
    HttpResponse response = client.execute(put);

    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    Assert.assertEquals(content, EntityUtils.toString(response.getEntity()));
    Assert.assertNotNull(response.getHeaders("X-Test-Header"));
    Assert.assertEquals(header, response.getHeaders("X-Test-Header")[0].getValue());
  }

  @Test
  public void testPost() throws Exception {
    String content = "post-content";
    String header = "post-header";

    DefaultHttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(String.format("http://%s:%d/dest/serve", hostname, servicePort));
    post.setHeader("X-Test-Header", header);
    post.setEntity(new StringEntity(content));
    HttpResponse response = client.execute(post);

    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    Assert.assertEquals(content, EntityUtils.toString(response.getEntity()));
    Assert.assertNotNull(response.getHeaders("X-Test-Header"));
    Assert.assertEquals(header, response.getHeaders("X-Test-Header")[0].getValue());
  }

  @Test
  public void testDelete() throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpDelete delete = new HttpDelete(String.format("http://%s:%d/dest/serve", hostname, servicePort));
    HttpResponse response = client.execute(delete);

    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    Assert.assertEquals("", EntityUtils.toString(response.getEntity()));
  }

  public static final class ServiceHandler extends AbstractHttpHandler {
    private final RequestForwarder forwarder = new RequestForwarder(new AsyncHttpClientConfig.Builder().build());

    @Override
    public void destroy(HandlerContext context) {
      forwarder.close();
    }

    @GET
    @Path("/.*")
    public void get(HttpRequest request, HttpResponder responder) {
      request.setUri(String.format("http://%s:%d%s", hostname, destServicePort, request.getUri()));
      forwarder.forward(request, responder);
    }

    @PUT
    @Path("/.*")
    public void put(HttpRequest request, HttpResponder responder) {
      request.setUri(String.format("http://%s:%d%s", hostname, destServicePort, request.getUri()));
      forwarder.forward(request, responder);
    }

    @POST
    @Path("/.*")
    public void post(HttpRequest request, HttpResponder responder) {
      request.setUri(String.format("http://%s:%d%s", hostname, destServicePort, request.getUri()));
      forwarder.forward(request, responder);
    }

    @DELETE
    @Path("/.*")
    public void delete(HttpRequest request, HttpResponder responder) {
      request.setUri(String.format("http://%s:%d%s", hostname, destServicePort, request.getUri()));
      forwarder.forward(request, responder);
    }
  }

  public static final class DestServiceHandler extends AbstractHttpHandler {
    @GET
    @Path("/dest/serve")
    public void get(HttpRequest request, HttpResponder responder) {
      Map<String, List<String>> queryParams = new QueryStringDecoder(request.getUri()).getParameters();

      if (queryParams.get("param1") != null && !queryParams.get("param1").isEmpty()) {
        responder.sendByteArray(HttpResponseStatus.OK, queryParams.get("param1").get(0).getBytes(Charsets.UTF_8),
                                ImmutableMultimap.of("X-Test-Header", request.getHeader("X-Test-Header"),
                                                     HttpHeaders.CONTENT_TYPE, "text/plain"));
      } else {
        responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      }
    }

    @PUT
    @Path("/dest/serve/{id}")
    public void put(HttpRequest request, HttpResponder responder, @PathParam("id") String id) {
      if (id.isEmpty()) {
        responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
        return;
      }

      responder.sendContent(HttpResponseStatus.OK, request.getContent(), "text/plain",
                            ImmutableMultimap.of("X-Test-Header", request.getHeader("X-Test-Header")));
    }

    @POST
    @Path("/dest/serve")
    public void post(HttpRequest request, HttpResponder responder) {
      responder.sendContent(HttpResponseStatus.OK, request.getContent(), "text/plain",
                              ImmutableMultimap.of("X-Test-Header", request.getHeader("X-Test-Header")));
    }

    @DELETE
    @Path("/dest/serve")
    public void delete(@SuppressWarnings("UnusedParameters") HttpRequest request, HttpResponder responder) {
      responder.sendStatus(HttpResponseStatus.OK);
    }

  }
}
