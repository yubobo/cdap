package com.continuuity.logging.context;

import com.continuuity.common.logging.ApplicationLoggingContext;

/**
 * User Service Logging Context.
 */
public class UserServiceLoggingContext extends ApplicationLoggingContext {

  public static final String TAG_USERAPP_ID = ".userappid";
  public static final String TAG_USERRUNNABLE_ID = ".userrunnableid";

  /**
   * Constructs the UserServiceLoggingContext
   * @param accountId account id
   * @param applicationId application id
   * @param twillAppId twill service id
   * @param twillRunnableId twill runnable id
   */
  public UserServiceLoggingContext(final String accountId,
                                   final String applicationId,
                                   final String twillAppId,
                                   final String twillRunnableId) {
    super(accountId, applicationId);
    setSystemTag(TAG_USERAPP_ID, twillAppId);
    setSystemTag(TAG_USERRUNNABLE_ID, twillRunnableId);
  }

  @Override
  public String getLogPartition() {
    return String.format("%s:%s", super.getLogPartition(), getSystemTag(TAG_USERAPP_ID));
  }

  @Override
  public String getLogPathFragment() {
    return String.format("%s/userservice-%s", super.getLogPathFragment(), getSystemTag(TAG_USERAPP_ID));
  }
}
