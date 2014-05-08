package com.continuuity.gateway.router.handlers;

import com.continuuity.common.conf.Constants;
import com.continuuity.security.auth.AccessTokenTransformer;
import com.continuuity.security.auth.TokenState;
import com.continuuity.security.auth.TokenValidator;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Security handler that intercept HTTP message and validates the access token in
 * header Authorization field.
 */
public class SecurityAuthenticationHttpHandler extends SimpleChannelHandler {
  private static final Logger LOG = LoggerFactory.getLogger(SecurityAuthenticationHttpHandler.class);

  private final TokenValidator tokenValidator;
  private final AccessTokenTransformer accessTokenTransformer;
  private DiscoveryServiceClient discoveryServiceClient;
  private Iterable<Discoverable> discoverables;
  private final String realm;

  private String clientIP;
  private String userName;
  private Date date;
  private String requestLine;
  private String responseCode;
  private String responseContentLength;

  private String auditLogLine;

  public SecurityAuthenticationHttpHandler(String realm, TokenValidator tokenValidator,
                                           AccessTokenTransformer accessTokenTransformer,
                                           DiscoveryServiceClient discoveryServiceClient) {
    this.realm = realm;
    this.tokenValidator = tokenValidator;
    this.accessTokenTransformer = accessTokenTransformer;
    this.discoveryServiceClient = discoveryServiceClient;
    discoverables = discoveryServiceClient.discover(Constants.Service.EXTERNAL_AUTHENTICATION);

    // default value of '-' in access log means the absence of that field
    this.clientIP = "-";
    this.userName = "-";
    this.requestLine = "-";
    this.responseCode = "-";
    this.responseContentLength = "-";
  }

  /**
   * Intercepts the HttpMessage for getting the access token in authorization header
   * @param ctx channel handler context delegated from MessageReceived callback
   * @param msg intercepted HTTP message
   * @param inboundChannel
   * @return {@code true} if the HTTP message has valid Access token
   * @throws Exception
   */
  private boolean validateSecuredInterception(ChannelHandlerContext ctx, HttpRequest msg,
                                      Channel inboundChannel) throws Exception {
    JsonObject jsonObject = new JsonObject();
    String auth = msg.getHeader(HttpHeaders.Names.AUTHORIZATION);
    String accessToken = null;

    //Parsing the access token from authorization header.The request authorization comes as
    //Authorization: Bearer accesstoken
    if (auth != null) {
      int spIndex = auth.trim().indexOf(' ');
      if (spIndex != -1) {
        accessToken = auth.substring(spIndex + 1).trim();
      }
    }

    date = new Date();
    clientIP = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
    requestLine = msg.getMethod() + " " + msg.getUri() + " " + msg.getProtocolVersion();

    TokenState tokenState = tokenValidator.validate(accessToken);
    if (!tokenState.isValid()) {
      HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
      if (tokenState == TokenState.MISSING) {
        httpResponse.addHeader(HttpHeaders.Names.WWW_AUTHENTICATE,
                                 String.format("Bearer realm=\"%s\"", realm));
        LOG.debug("Failed authentication due to missing token");

      } else {
        httpResponse.addHeader(HttpHeaders.Names.WWW_AUTHENTICATE,
                                 String.format("Bearer realm=\"%s\" error=\"invalid_token\"" +
                                                 "error_description=\"%s\"", realm, tokenState.getMsg()));
        jsonObject.addProperty("error", "invalid_token");
        jsonObject.addProperty("error_description", tokenState.getMsg());
        LOG.debug("Failed authentication due to invalid token, reason={};", tokenState);
      }
      responseCode = "401";
      JsonArray externalAuthenticationURIs = new JsonArray();

      //Waiting for service to get discovered
      stopWatchWait(externalAuthenticationURIs);

      jsonObject.add("auth_uri", externalAuthenticationURIs);

      ChannelBuffer content = ChannelBuffers.wrappedBuffer(jsonObject.toString().getBytes(Charsets.UTF_8));
      httpResponse.setContent(content);
      httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
      httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json;charset=UTF-8");
      responseContentLength = "" + content.readableBytes();
      ChannelFuture writeFuture = Channels.future(inboundChannel);
      Channels.write(ctx, writeFuture, httpResponse);
      writeFuture.addListener(ChannelFutureListener.CLOSE);
      return false;
    } else {
        AccessTokenTransformer.AccessTokenIdentifierPair accessTokenIdentifierPair =
        accessTokenTransformer.transform(accessToken);
        userName = accessTokenIdentifierPair.getAccessTokenIdentifierObj().getUsername();
        msg.setHeader(HttpHeaders.Names.WWW_AUTHENTICATE, "Reactor-verified " +
                                                          accessTokenIdentifierPair.getAccessTokenIdentifierStr());
        return true;
    }
  }

  /**
   *
   * @param externalAuthenticationURIs the list that should be populated with discovered with
   *                                   external auth servers URIs
   * @throws Exception
   */
  private void stopWatchWait(JsonArray externalAuthenticationURIs) throws Exception {
    boolean done = false;
    Stopwatch stopwatch = new Stopwatch();
    stopwatch.start();
    do {
      for (Discoverable d : discoverables)  {
        externalAuthenticationURIs.add(new JsonPrimitive(d.getSocketAddress().getHostName()));
        done = true;
      }
      if (!done) {
        TimeUnit.MILLISECONDS.sleep(200);
      }
    } while (!done && stopwatch.elapsedTime(TimeUnit.SECONDS) < 2L);
  }


  @Override
  public void messageReceived(ChannelHandlerContext ctx, final MessageEvent event) throws Exception {
    Object msg = event.getMessage();
    if (!(msg instanceof HttpRequest)) {
      super.messageReceived(ctx, event);
    } else if (validateSecuredInterception(ctx, (HttpRequest) msg, event.getChannel())) {
      System.out.println(getAuditLogLine());
      Channels.fireMessageReceived(ctx, msg, event.getRemoteAddress());
    } else {
      System.out.println(getAuditLogLine());
      return;
    }
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ChannelBuffer channelBuffer = (ChannelBuffer) e.getMessage();
    ChannelBuffer sliced = channelBuffer.slice(channelBuffer.readerIndex(), channelBuffer.readableBytes());
    byte b = ' ';
    int indx = sliced.indexOf(sliced.readerIndex(), sliced.readableBytes(), b);
    responseCode = sliced.slice(indx, 4).toString(Charsets.UTF_8);
    System.out.println(channelBuffer.toString(Charsets.UTF_8));

    System.out.println(getAuditLogLine());
    super.writeRequested(ctx, e);
  }

  private String getAuditLogLine() {
    return String.format("%s %s [%s] %s %s %s", clientIP, userName, date,
                         requestLine, responseCode, responseContentLength);
  }

}
