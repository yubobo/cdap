package com.continuuity.examples.dataset;

import java.util.Arrays;

/**
 * Wraps byte[] and provides equals() and hashCode() implementations.
 */
public final class ByteArrayWrapper {
  private final byte[] data;

  public ByteArrayWrapper(byte[] data) {
    if (data == null) {
      throw new NullPointerException();
    }
    this.data = data;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ByteArrayWrapper)) {
      return false;
    }
    return Arrays.equals(data, ((ByteArrayWrapper) other).data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }
}
