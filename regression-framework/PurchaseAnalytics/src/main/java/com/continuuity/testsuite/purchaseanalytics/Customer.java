package com.continuuity.testsuite.purchaseanalytics;

/**
 *
 */
public class Customer extends SerializedObject {
  private final String name;
  private final short zip;
  private final short rating;

  public String getName() {
    return name;
  }

  public short getZip() {
    return zip;
  }

  public short getRating() {
    return rating;
  }

  public Customer(String name, short zip, short rating) {
    super();
    this.name = name;
    this.zip = zip;
    this.rating = rating;
  }
}
