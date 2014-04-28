package com.continuuity.security.auth;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

/**
 *
 */
public class AccessTokenTransformer {
  private final TokenManager tokenManager;
  private final Codec<AccessToken> accessTokenCodec;
  private final Codec<AccessTokenIdentifier> accessTokenIdentifierCodec;

  @Inject
  public AccessTokenTransformer(TokenManager tokenManager, Codec<AccessToken> accessTokenCodec,
                                Codec<AccessTokenIdentifier> accessTokenIdentifierCodec) {
    this.tokenManager = tokenManager;
    this.accessTokenCodec = accessTokenCodec;
    this.accessTokenIdentifierCodec = accessTokenIdentifierCodec;
  }

  public String transform(String accessToken) throws IOException{
    String accessTokenIdentifier = null;
    byte[] decodedAccessToken = Base64.decodeBase64(accessToken);
    AccessToken accessTokenObj = accessTokenCodec.decode(decodedAccessToken);
    AccessTokenIdentifier accessTokenIdentifierObj = accessTokenObj.getIdentifier();
    byte[] encodedAccessTokenIdentifier = accessTokenIdentifierCodec.encode(accessTokenIdentifierObj);
    accessTokenIdentifier = new String(encodedAccessTokenIdentifier, Charsets.UTF_8);
    return accessTokenIdentifier;
  }
}
