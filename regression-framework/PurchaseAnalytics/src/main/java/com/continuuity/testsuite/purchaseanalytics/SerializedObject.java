package com.continuuity.testsuite.superpurchase;

import java.util.UUID;

/**
 *
 */
public class SerializedObject {
  private final UUID uuid;

  public UUID getUuid() {
    return uuid;
  }

  public SerializedObject() {
    this.uuid = UUID.randomUUID();
  }
}
