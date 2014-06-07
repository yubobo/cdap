package com.continuuity.common.security;

/**
 * RequestContext that maintains a ThreadLocal with references to {@code AccessTokenIdentifier}.
 */
public class SecurityRequestContext {
  private static final ThreadLocal<AbstractAccessTokenIdentifier> threadLocal =
                                                          new InheritableThreadLocal<AbstractAccessTokenIdentifier>();

  /**
   * Get the {@code AccessTokenIdentifier} set on the current thread.
   * @return
   */
  public static AbstractAccessTokenIdentifier get() {
    return threadLocal.get();
  }

  /**
   * Set the {@code AccessTokenIdentifier} on the current thread.
   * @param abstractAccessTokenIdentifier
   */
  public static void set(AbstractAccessTokenIdentifier abstractAccessTokenIdentifier) {
    threadLocal.set(abstractAccessTokenIdentifier);
  }
}
