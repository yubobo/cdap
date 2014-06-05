package com.continuuity.security.auth;

/**
 * RequestContext that maintains a ThreadLocal with references to {@code AccessTokenIdentifier}.
 */
public class SecurityRequestContext {
  private static final ThreadLocal<AccessTokenIdentifier> threadLocal =
                                                                    new InheritableThreadLocal<AccessTokenIdentifier>();

  /**
   * Get the {@code AccessTokenIdentifier} set on the current thread.
   * @return
   */
  public static AccessTokenIdentifier get() {
    return threadLocal.get();
  }

  /**
   * Set the {@code AccessTokenIdentifier} on the current thread.
   * @param accessTokenIdentifier
   */
  public static void set(AccessTokenIdentifier accessTokenIdentifier) {
    threadLocal.set(accessTokenIdentifier);
  }
}
