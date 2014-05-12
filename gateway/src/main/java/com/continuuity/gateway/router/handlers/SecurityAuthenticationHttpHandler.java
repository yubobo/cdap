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
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Security handler that intercept HTTP message and validates the access token in
 * header Authorization field.
 */
public class SecurityAuthenticationHttpHandler extends SimpleChannelHandler {
  private static final Logger LOG = LoggerFactory.getLogger(SecurityAuthenticationHttpHandler.class);
  private static final Logger AUDIT_LOG = LoggerFactory.getLogger("http-access");

  private final TokenValidator tokenValidator;
  private final AccessTokenTransformer accessTokenTransformer;
  private DiscoveryServiceClient discoveryServiceClient;
  private Iterable<Discoverable> discoverables;
  private final String realm;
  private DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");


  public SecurityAuthenticationHttpHandler(String realm, TokenValidator tokenValidator,
                                           AccessTokenTransformer accessTokenTransformer,
                                           DiscoveryServiceClient discoveryServiceClient) {
    this.realm = realm;
    this.tokenValidator = tokenValidator;
    this.accessTokenTransformer = accessTokenTransformer;
    this.discoveryServiceClient = discoveryServiceClient;
    discoverables = discoveryServiceClient.discover(Constants.Service.EXTERNAL_AUTHENTICATION);
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
                                      Channel inboundChannel, AuditLogEntry logEntry) throws Exception {
    JsonObject jsonObject = new JsonObject();
    String auth = msg.getHeader(HttpHeaders.Names.AUTHORIZATION);
    String accessToken = null;

    /*
     * Parse the access token from authorization header.  The header will be in the form:
     *     Authorization: Bearer ACCESSTOKEN
     *
     * where ACCESSTOKEN is the base64 encoded serialized AccessToken instance.
     */
    if (auth != null) {
      int spIndex = auth.trim().indexOf(' ');
      if (spIndex != -1) {
        accessToken = auth.substring(spIndex + 1).trim();
      }
    }

    logEntry.clientIP = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
    logEntry.requestLine = msg.getMethod() + " " + msg.getUri() + " " + msg.getProtocolVersion();

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
                                                 " error_description=\"%s\"", realm, tokenState.getMsg()));
        jsonObject.addProperty("error", "invalid_token");
        jsonObject.addProperty("error_description", tokenState.getMsg());
        LOG.debug("Failed authentication due to invalid token, reason={};", tokenState);
      }
      logEntry.responseCode = "401";
      JsonArray externalAuthenticationURIs = new JsonArray();

      //Waiting for service to get discovered
      stopWatchWait(externalAuthenticationURIs);

      jsonObject.add("auth_uri", externalAuthenticationURIs);

      ChannelBuffer content = ChannelBuffers.wrappedBuffer(jsonObject.toString().getBytes(Charsets.UTF_8));
      httpResponse.setContent(content);
      httpResponse.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
      httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json;charset=UTF-8");
      logEntry.responseContentLength = "" + content.readableBytes();
      ChannelFuture writeFuture = Channels.future(inboundChannel);
      Channels.write(ctx, writeFuture, httpResponse);
      writeFuture.addListener(ChannelFutureListener.CLOSE);
      return false;
    } else {
        AccessTokenTransformer.AccessTokenIdentifierPair accessTokenIdentifierPair =
        accessTokenTransformer.transform(accessToken);
        logEntry.userName = accessTokenIdentifierPair.getAccessTokenIdentifierObj().getUsername();
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
    } else {
      AuditLogEntry logEntry = new AuditLogEntry();
      ctx.setAttachment(logEntry);
      if (validateSecuredInterception(ctx, (HttpRequest) msg, event.getChannel(), logEntry)) {
        Channels.fireMessageReceived(ctx, msg, event.getRemoteAddress());
      } else {
        AUDIT_LOG.trace(logEntry.toString());
        return;
      }
    }
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    AuditLogEntry logEntry = getLogEntry(ctx);
    Object message = e.getMessage();
    if (message instanceof HttpResponse) {
      HttpResponse response = (HttpResponse) message;
      logEntry.responseCode = Integer.toString(response.getStatus().getCode());
    } else if (message instanceof ChannelBuffer) {
      ChannelBuffer channelBuffer = (ChannelBuffer) message;
      ChannelBuffer sliced = channelBuffer.slice(channelBuffer.readerIndex(), channelBuffer.readableBytes());
      byte b = ' ';
      int indx = sliced.indexOf(sliced.readerIndex(), sliced.readableBytes(), b);
      logEntry.responseCode = sliced.slice(indx, 4).toString(Charsets.UTF_8);
    } else {
      LOG.info("Response message is of type " + message.getClass());
    }
    AUDIT_LOG.trace(logEntry.toString());
    super.writeRequested(ctx, e);
  }

  private AuditLogEntry getLogEntry(ChannelHandlerContext ctx) {
    Object entryObject = ctx.getAttachment();
    AuditLogEntry logEntry;
    if (entryObject != null && entryObject instanceof AuditLogEntry) {
      logEntry = (AuditLogEntry) entryObject;
    } else {
      logEntry = new AuditLogEntry();
      ctx.setAttachment(logEntry);
    }
    return logEntry;
  }

  private final class AuditLogEntry {
    /** Each audit log field will default to "-" if the field is missing or not supported. */
    private static final String DEFAULT_VALUE = "-";

    private String clientIP = DEFAULT_VALUE;
    private String userName = DEFAULT_VALUE;
    private Date date;
    private String requestLine = DEFAULT_VALUE;
    private String responseCode = DEFAULT_VALUE;
    private String responseContentLength = DEFAULT_VALUE;

    public AuditLogEntry() {
      this.date = new Date();
    }

    public String toString() {
      return String.format("%s %s [%s] \"%s\" %s %s", clientIP, userName, dateFormat.format(date),
                           requestLine, responseCode, responseContentLength);
    }
  }
}
