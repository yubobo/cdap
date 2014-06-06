package com.continuuity.gateway.auth;

import com.continuuity.common.security.AccessTokenIdentifierInterface;
import com.continuuity.common.security.SecurityRequestContext;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 *
 */
public class ReactorAuthenticator implements Authenticator {

  @Override
  public boolean isAuthenticationRequired() {
    return true;
  }

  @Override
  public boolean authenticateRequest(HttpRequest httpRequest) {
    AccessTokenIdentifierInterface accessTokenIdentifier = SecurityRequestContext.get();
    if (accessTokenIdentifier == null) {
      throw new SecurityException("No authenticationToken was set on current request");
    }
    return true;
  }

  @Override
  public boolean authenticateRequest(AvroFlumeEvent event) {
    throw new SecurityException("Flume events aren't supported.");
  }

  @Override
  public String getAccountId(HttpRequest httpRequest) {
    AccessTokenIdentifierInterface accessTokenIdentifier = SecurityRequestContext.get();
    if (accessTokenIdentifier == null) {
      throw new SecurityException("No authenticationToken was set on current request");
    }
    return accessTokenIdentifier.getUsername();
  }

  @Override
  public String getAccountId(AvroFlumeEvent event) {
    throw new SecurityException("Flume events aren't supported.");
  }
}
