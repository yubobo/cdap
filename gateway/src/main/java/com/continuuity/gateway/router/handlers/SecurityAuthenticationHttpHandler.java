package com.continuuity.gateway.router.handlers;


import com.continuuity.common.conf.Constants;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.common.guice.IOModule;
import com.continuuity.security.auth.AccessTokenTransformer;
import com.continuuity.security.auth.TokenValidator;
import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.util.Iterator;

/**
 * Security handler that intercept HTTP message and validates the access token in
 * header Authorization field
 */
public class SecurityAuthenticationHttpHandler extends SimpleChannelUpstreamHandler {
  private TokenValidator tokenValidator;
  private AccessTokenTransformer accessTokenTransformer;
  DiscoveryServiceClient discoveryServiceClient;
  private boolean securityEnabled;
  private String realm;

  public SecurityAuthenticationHttpHandler(String realm, TokenValidator tokenValidator,
                                           AccessTokenTransformer accessTokenTransformer, boolean securityEnabled,
                                           DiscoveryServiceClient discoveryServiceClient) {
    this.realm = realm;
    this.tokenValidator = tokenValidator;
    this.accessTokenTransformer = accessTokenTransformer;
    this.securityEnabled = securityEnabled;
    this.discoveryServiceClient = discoveryServiceClient;
  }


  private void securedInterception(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
    final HttpRequest msg = (HttpRequest) event.getMessage();
    JsonObject jsonObject = new JsonObject();
    // Suspend incoming traffic until connected to the outbound service.
    final Channel inboundChannel = event.getChannel();
    inboundChannel.setReadable(false);

    String auth = msg.getHeader(HttpHeaders.Names.AUTHORIZATION);
    String accessToken = null;

    if (auth != null) {
      int spIndex = auth.trim().indexOf(' ') + 1;
      if (spIndex != -1) {
        accessToken = auth.substring(spIndex).trim();
      }
    }
    TokenValidator.State tokenState = tokenValidator.validate(accessToken);
    HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
    switch (tokenState) {
      case TOKEN_MISSING:
        httpResponse.addHeader(HttpHeaders.Names.WWW_AUTHENTICATE, "Bearer realm=\"" + realm + "\"");
        jsonObject.addProperty("error", "Token Missing");
        jsonObject.addProperty("error_description", tokenState.getMsg());
        break;

      case TOKEN_INVALID:
      case TOKEN_EXPIRED:
      case TOKEN_INTERNAL:
        httpResponse.addHeader(HttpHeaders.Names.WWW_AUTHENTICATE, "Bearer realm=\"" + realm + "\"" +
          "  error=\"invalid_token\"" +
          "  error_description=\"" + tokenState.getMsg() + "\"");
        jsonObject.addProperty("error", "invalid_token");
        jsonObject.addProperty("error_description", tokenState.getMsg());
        break;
    }
    if (tokenState != TokenValidator.State.TOKEN_VALID) {
      Iterable<Discoverable> discoverables = discoveryServiceClient.discover(Constants.Service.EXTERNAL_AUTHENTICATION);
      Iterator<Discoverable> discoverableIterator = discoverables.iterator();
      JsonArray externalAuthenticationURIs = new JsonArray();
      while (discoverableIterator.hasNext()) {
        Discoverable d = discoverableIterator.next();
        externalAuthenticationURIs.add(new JsonPrimitive(d.getSocketAddress().getHostName()));
      }
      jsonObject.add("auth_uri", externalAuthenticationURIs);

      ChannelBuffer content = ChannelBuffers.wrappedBuffer(jsonObject.toString().getBytes(Charsets.UTF_8));
      httpResponse.setContent(content);
      httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
      httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json;charset=UTF-8");

      ChannelFuture writeFuture = Channels.future(event.getChannel());
      Channels.write(ctx, writeFuture, httpResponse);
      writeFuture.addListener(ChannelFutureListener.CLOSE);
      return;
    } else {
      //TODO: transform the access token and send the access token identifier
      // in request with modified Authorization header
//      String serealizedAccessTokenIdentifier = accessTokenTransformer.transform(accessToken);
//      msg.setHeader(HttpHeaders.Names.WWW_AUTHENTICATE, "Reactor-verified " + serealizedAccessTokenIdentifier);
      Channels.fireMessageReceived(ctx, msg, event.getRemoteAddress());
    }
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, final MessageEvent event) throws Exception {
    HttpRequest msg = (HttpRequest) event.getMessage();
    if (event.getMessage() instanceof HttpChunk) {
      Channels.fireMessageReceived(ctx, event.getMessage(), event.getRemoteAddress());
    } else if (securityEnabled) {
        securedInterception(ctx, event);

    } else {
      Channels.fireMessageReceived(ctx, event.getMessage(), event.getRemoteAddress());
    }
  }
}
