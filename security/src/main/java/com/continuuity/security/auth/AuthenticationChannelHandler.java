package com.continuuity.security.auth;

import com.continuuity.common.conf.Constants;
import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * An UpstreamHandler that verifies the AccessTokenIdentifier in a Request header and
 * sets a {@code SecurityRequestContext}.
 */
public class AuthenticationChannelHandler extends SimpleChannelUpstreamHandler {
  public static final String HANDLER_NAME = "authenticator";
  private final AccessTokenIdentifierCodec accessTokenIdentifierCodec;

  @Inject
  public AuthenticationChannelHandler(AccessTokenIdentifierCodec accessTokenIdentifierCodec) {
    this.accessTokenIdentifierCodec = accessTokenIdentifierCodec;
  }

  /**
   * Decode the AccessTokenIdentifier passed as a header and set it in a ThreadLocal.
   * Returns a 401 if the identifier is malformed. 
   * @param ctx
   * @param e
   * @throws Exception
   */
  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Object message = e.getMessage();
    if (!(message instanceof HttpRequest)) {
      super.messageReceived(ctx, e);
      return;
    }
    HttpRequest request = (HttpRequest) message;
    String header = request.getHeader(HttpHeaders.Names.AUTHORIZATION);
    header = header.replaceFirst(Constants.Security.VERIFIED_HEADER_BASE, "");
    byte[] encodedAccessTokenIdentifier = Base64.decodeBase64(header);
    try {
      AccessTokenIdentifier accessTokenIdentifier = accessTokenIdentifierCodec.decode(encodedAccessTokenIdentifier);
      SecurityRequestContext.set(accessTokenIdentifier);
      super.messageReceived(ctx, e);
    } catch (Exception ex) {
      exceptionCaught(ctx, new DefaultExceptionEvent(ctx.getChannel(), ex.getCause()));
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    ChannelFuture future = Channels.future(ctx.getChannel());
    future.addListener(ChannelFutureListener.CLOSE);
    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
    Channels.write(ctx, future, response);
  }
}
