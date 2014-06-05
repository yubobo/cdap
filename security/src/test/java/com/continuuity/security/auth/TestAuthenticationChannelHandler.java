package com.continuuity.security.auth;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.common.guice.IOModule;
import com.continuuity.http.HandlerContext;
import com.continuuity.http.HttpHandler;
import com.continuuity.http.HttpResponder;
import com.continuuity.http.NettyHttpService;
import com.continuuity.security.guice.SecurityModules;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.codec.binary.Base64;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Tests for {@code AuthenticationChannelHandler}.
 */
public class TestAuthenticationChannelHandler {
  private static NettyHttpService httpService;
  private static CConfiguration configuration;
  private static AccessTokenIdentifierCodec accessTokenIdentifierCodec;
  private static AuthenticationChannelHandler authenticationHandler;
  private static URI baseURI;

  @BeforeClass
  public static void setup() {
    Injector injector = Guice.createInjector(new IOModule(), new ConfigModule(),
                                             new DiscoveryRuntimeModule().getInMemoryModules(),
                                             new SecurityModules().getInMemoryModules());

    configuration = injector.getInstance(CConfiguration.class);
    authenticationHandler = injector.getInstance(AuthenticationChannelHandler.class);
    accessTokenIdentifierCodec = injector.getInstance(AccessTokenIdentifierCodec.class);
    httpService = NettyHttpService.builder()
                  .setHost(configuration.get(Constants.Security.AUTH_SERVER_ADDRESS))
                  .addHttpHandlers(ImmutableList.of(new TestHandler()))
                  .modifyChannelPipeline(getChannelModifier())
                  .build();
    httpService.startAndWait();
    baseURI = URI.create(String.format("http://%s:%d", httpService.getBindAddress().getHostName(),
                                                       httpService.getBindAddress().getPort()));
  }

  private static Function getChannelModifier() {
    return new Function<ChannelPipeline, ChannelPipeline>() {
      @Nullable
      @Override
      public ChannelPipeline apply(@Nullable ChannelPipeline input) {
        input.addAfter("decoder", AuthenticationChannelHandler.HANDLER_NAME, authenticationHandler);
        return input;
      }
    };
  }

  private static final class TestUserInfo {
    static final String USERNAME = "test";
    static final Collection<String> GROUPS = Collections.emptyList();
    static final long ISSUE_TIME = System.currentTimeMillis();
    static final long EXPIRY_TIME = ISSUE_TIME + configuration.getInt(Constants.Security.TOKEN_EXPIRATION);
  }

  /**
   * Test that a valid AccessTokenIdentifier is set correctly on a ThreadLocal.
   * @throws Exception
   */
  @Test
  public void testValidAccessTokenIdentifier() throws Exception {
    URL url = baseURI.resolve("/test/token").toURL();
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

    AccessTokenIdentifier accessTokenIdentifier = new AccessTokenIdentifier(TestUserInfo.USERNAME, TestUserInfo.GROUPS,
                                                                            TestUserInfo.ISSUE_TIME,
                                                                            TestUserInfo.EXPIRY_TIME);
    byte[] encodedAccessTokenIdentifier = accessTokenIdentifierCodec.encode(accessTokenIdentifier);
    String base64EncodedIdentifier = Base64.encodeBase64String(encodedAccessTokenIdentifier).trim();
    urlConn.setRequestProperty(HttpHeaders.Names.AUTHORIZATION,
                               Constants.Security.VERIFIED_HEADER_BASE + base64EncodedIdentifier);

    urlConn.setRequestMethod(HttpMethod.GET.getName());
    urlConn.setRequestProperty(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);

    Assert.assertEquals(200, urlConn.getResponseCode());
  }

  /**
   * Test that an invalid AccessTokenIdentifier in a Request header returns a 401.
   * @throws Exception
   */
  @Test
  public void testInvalidAccessTokenIdentifier() throws Exception {
    URL url = baseURI.resolve("/test/token").toURL();
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

    urlConn.setRequestProperty(HttpHeaders.Names.AUTHORIZATION, Constants.Security.VERIFIED_HEADER_BASE + "xxxxx");

    urlConn.setRequestMethod(HttpMethod.GET.getName());
    urlConn.setRequestProperty(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
    Assert.assertEquals(401, urlConn.getResponseCode());
  }

  @Path("/test")
  public static class TestHandler implements HttpHandler {

    @Path("token")
    @GET
    public void testGet(HttpRequest request, HttpResponder responder) {
      JsonObject object = new JsonObject();
      AccessTokenIdentifier identifier = SecurityRequestContext.get();
      if (identifier == null) {
        responder.sendError(HttpResponseStatus.UNAUTHORIZED, "No AccessTokenIdentifier was found.");
      }

      Assert.assertEquals(identifier.getUsername(), TestUserInfo.USERNAME);
      Assert.assertEquals(identifier.getGroups(), TestUserInfo.GROUPS);
      Assert.assertEquals(identifier.getIssueTimestamp(), TestUserInfo.ISSUE_TIME);
      Assert.assertEquals(identifier.getExpireTimestamp(), TestUserInfo.EXPIRY_TIME);
      responder.sendJson(HttpResponseStatus.OK, object);
    }

    @Override
    public void init(HandlerContext context) {
    }

    @Override
    public void destroy(HandlerContext context) {
    }
  }

}
