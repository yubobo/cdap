package com.continuuity.common.security;

import com.continuuity.internal.io.Schema;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Inteface that AccessTokenIdentifiers implement.
 */
public interface AccessTokenIdentifierInterface {

  /**
   * Schema of AccessTokenIdentifiers.
   */
  static final class Schemas {
    private static final int VERSION = 1;
    private static final Map<Integer, Schema> schemas = Maps.newHashMap();
    static {
      schemas.put(1, Schema.recordOf("AccessTokenIdentifier",
                                     Schema.Field.of("username", Schema.of(Schema.Type.STRING)),
                                     Schema.Field.of("groups", Schema.arrayOf(Schema.of(Schema.Type.STRING))),
                                     Schema.Field.of("issueTimestamp", Schema.of(Schema.Type.LONG)),
                                     Schema.Field.of("expireTimestamp", Schema.of(Schema.Type.LONG))));
    }

    public static int getVersion() {
      return VERSION;
    }

    public static Schema getSchemaVersion(int version) {
      return schemas.get(version);
    }

    public static Schema getCurrentSchema() {
      return schemas.get(VERSION);
    }
  }

  public String getUsername();

  public List<String> getGroups();

  public long getIssueTimestamp();

  public long getExpireTimestamp();

}
