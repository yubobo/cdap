package com.continuuity.common.security;

import java.util.List;

/**
 * Inteface that AccessTokenIdentifiers implement.
 */
public interface AbstractAccessTokenIdentifier {

  public abstract String getUsername();

  public abstract List<String> getGroups();

  public abstract long getIssueTimestamp();

  public abstract long getExpireTimestamp();

}
