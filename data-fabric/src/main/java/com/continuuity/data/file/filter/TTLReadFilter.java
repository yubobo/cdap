/*
 * Copyright 2014 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data.file.filter;

import com.continuuity.data.file.ReadFilter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * {@link com.continuuity.data.file.ReadFilter} for filtering expired stream events according to TTL and current time.
 */
public class TTLReadFilter extends ReadFilter {

  /**
   * Time to live.
   */
  private Supplier<Long> ttl;

  public TTLReadFilter(long ttl) {
    this.ttl = Suppliers.ofInstance(ttl);
  }

  public TTLReadFilter(Supplier<Long> ttl) {
    this.ttl = ttl;
  }

  public long getTTL() {
    return ttl.get();
  }

  @Override
  public boolean acceptTimestamp(long timestamp) {
    return getCurrentTime() - timestamp <= ttl.get();
  }

  protected long getCurrentTime() {
    return System.currentTimeMillis();
  }
}
