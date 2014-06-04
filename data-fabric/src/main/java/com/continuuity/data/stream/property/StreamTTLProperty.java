package com.continuuity.data.stream.property;

/**
 * Object for holding property value in the property store.
 */
public final class StreamTTLProperty {

  private final long ttl;

  public StreamTTLProperty(long ttl) {
    this.ttl = ttl;
  }

  public long getTTL() {
    return ttl;
  }
}
