package com.continuuity.security.auth;

import com.continuuity.common.security.AbstractAccessTokenIdentifier;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

/**
 * Represents a verified user identity.
 */
public class AccessTokenIdentifier extends AbstractAccessTokenIdentifier {

  private final String username;
  private final List<String> groups;
  private final long issueTimestamp;
  private final long expireTimestamp;

  public AccessTokenIdentifier(String username, Collection<String> groups, long issueTimestamp,
                               long expireTimestamp) {
    this.username = username;
    this.groups = ImmutableList.copyOf(groups);
    this.issueTimestamp = issueTimestamp;
    this.expireTimestamp = expireTimestamp;
  }

  /**
   * Returns the username for this identity.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the list of verified group memberships for this user identity.
   */
  public List<String> getGroups() {
    return groups;
  }

  /**
   * Returns the timestamp, in milliseconds, when this token was issued.
   */
  public long getIssueTimestamp() {
    return issueTimestamp;
  }

  /**
   * Returns the timestamp, in milliseconds, when this token will expire.
   */
  public long getExpireTimestamp() {
    return expireTimestamp;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || !(other instanceof AccessTokenIdentifier)) {
      return false;
    }
    AccessTokenIdentifier otherToken = (AccessTokenIdentifier) other;

    return Objects.equal(username, otherToken.username) &&
      Objects.equal(groups, otherToken.groups) &&
      issueTimestamp == otherToken.issueTimestamp &&
      expireTimestamp == otherToken.expireTimestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getUsername(),
                            getGroups(),
                            getIssueTimestamp(),
                            getExpireTimestamp());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("username", username)
      .add("groups", groups)
      .add("issueTimestamp", issueTimestamp)
      .add("expireTimestamp", expireTimestamp)
      .toString();
  }
}
