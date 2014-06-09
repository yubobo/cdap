package com.continuuity.gateway.auth;

import com.continuuity.common.conf.Constants;
import com.continuuity.common.io.BinaryDecoder;
import com.continuuity.common.io.Decoder;
import com.continuuity.common.security.AbstractAccessTokenIdentifier;
import com.continuuity.internal.io.DatumReader;
import com.continuuity.internal.io.DatumReaderFactory;
import com.continuuity.internal.io.ReflectionDatumReaderFactory;
import com.continuuity.internal.io.Schema;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 */
public class ReactorAuthenticator implements Authenticator {
  private static final TypeToken<AbstractAccessTokenIdentifier> ACCESS_TOKEN_IDENTIFIER_TYPE =
    new TypeToken<AbstractAccessTokenIdentifier>() { };

  @Override
  public boolean isAuthenticationRequired() {
    return true;
  }

  @Override
  public boolean authenticateRequest(HttpRequest httpRequest) {
    String header = httpRequest.getHeader(HttpHeaders.Names.AUTHORIZATION);
    header = header.replaceFirst(Constants.Security.VERIFIED_HEADER_BASE, "");
    byte[] encodedAccessTokenIdentifier = Base64.decodeBase64(header);
    try {
      decode(encodedAccessTokenIdentifier);
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
    String header = httpRequest.getHeader(HttpHeaders.Names.AUTHORIZATION);
    header = header.replaceFirst(Constants.Security.VERIFIED_HEADER_BASE, "");
    byte[] encodedAccessTokenIdentifier = Base64.decodeBase64(header);
    try {
      AbstractAccessTokenIdentifier accessTokenIdentifier = decode(encodedAccessTokenIdentifier);
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

  public AbstractAccessTokenIdentifier decode(byte[] data) throws IOException {
    DatumReaderFactory readerFactory = new ReflectionDatumReaderFactory();
    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    Decoder decoder = new BinaryDecoder(bis);
    DatumReader<AbstractAccessTokenIdentifier> reader = readerFactory.create(ACCESS_TOKEN_IDENTIFIER_TYPE,
                                                              AbstractAccessTokenIdentifier.Schemas.getCurrentSchema());
    int readVersion = decoder.readInt();
    Schema readSchema = AbstractAccessTokenIdentifier.Schemas.getSchemaVersion(readVersion);
    if (readSchema == null) {
      throw new IOException("Unknown schema version for AccessTokenIdentifier: " + readVersion);
    }
    return reader.read(decoder, readSchema);
  }
}
