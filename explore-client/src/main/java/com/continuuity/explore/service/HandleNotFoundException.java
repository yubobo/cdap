package com.continuuity.explore.service;

/**
 * Exception thrown when {@link com.continuuity.api.metadata.QueryHandle} is not found.
 */
public class HandleNotFoundException extends Exception {
  public HandleNotFoundException(String s) {
    super(s);
  }
}
