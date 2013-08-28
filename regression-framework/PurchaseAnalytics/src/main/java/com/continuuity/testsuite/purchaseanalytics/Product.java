package com.continuuity.testsuite.superpurchase;

/**
 *
 */
public class Product extends SerializedObject {
  private final String description;

  public  String getDescription() {
    return description;
  }

  public Product(String description) {
    super();
    this.description = description;
  }
}
