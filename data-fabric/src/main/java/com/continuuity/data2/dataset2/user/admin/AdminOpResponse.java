package com.continuuity.data2.dataset2.user.admin;

/**
 * TODO: improve this response.
 */
public class AdminOpResponse {
  private Object result;
  private boolean success;
  private String message;

  public AdminOpResponse(Object result, boolean success, String message) {
    this.result = result;
    this.success = success;
    this.message = message;
  }

  public Object getResult() {
    return result;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }
}