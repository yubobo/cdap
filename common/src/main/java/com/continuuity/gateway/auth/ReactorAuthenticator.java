package com.continuuity.gateway.auth;

import com.continuuity.common.conf.Constants;
import com.continuuity.common.io.Codec;
import com.continuuity.common.security.AccessTokenIdentifier;
import com.google.common.base.Throwables;
import org.apache.commons.codec.binary.Base64;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 *
 */
public class ReactorAuthenticator implements Authenticator {
  private final Codec<AccessTokenIdentifier> accessTokenIdentifierCodec;

  public ReactorAuthenticator(Codec<AccessTokenIdentifier> accessTokenIdentifierCodec) {
    this.accessTokenIdentifierCodec = accessTokenIdentifierCodec;
  }

  @Override
  public boolean isAuthenticationRequired() {
    return true;
  }

  @Override
  public boolean authenticateRequest(HttpRequest httpRequest) {
    try {
      String header = httpRequest.getHeader(HttpHeaders.Names.AUTHORIZATION);
      header = header.replaceFirst(Constants.Security.VERIFIED_HEADER_BASE, "");
      byte[] encodedAccessTokenIdentifier = Base64.decodeBase64(header);
      accessTokenIdentifierCodec.decode(encodedAccessTokenIdentifier);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  @Override
  public boolean authenticateRequest(AvroFlumeEvent event) {
    return false;
  }

  @Override
  public String getAccountId(HttpRequest httpRequest) {
    try {
      String header = httpRequest.getHeader(HttpHeaders.Names.AUTHORIZATION);
      header = header.replaceFirst(Constants.Security.VERIFIED_HEADER_BASE, "");
      byte[] encodedAccessTokenIdentifier = Base64.decodeBase64(header);
      AccessTokenIdentifier accessTokenIdentifier = accessTokenIdentifierCodec.decode(encodedAccessTokenIdentifier);
      return accessTokenIdentifier.getUsername();
    } catch (Exception ex) {
      Throwables.propagate(ex);
    }
    return null;
  }

  @Override
  public String getAccountId(AvroFlumeEvent event) {
    return null;
  }

}
