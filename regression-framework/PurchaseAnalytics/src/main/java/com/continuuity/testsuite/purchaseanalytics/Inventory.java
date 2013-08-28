package com.continuuity.testsuite.superpurchase;

/**
 *
 */
public class Inventory extends SerializedObject {
  private final long quantity;

  public long getQuantity() {
    return quantity;
  }

  public Inventory(long quantity) {
    this.quantity = quantity;
  }
}
