package com.continuuity.common.security;

/**
 * RequestContext that maintains a ThreadLocal with references to {@code AccessTokenIdentifier}.
 */
public class SecurityRequestContext {
  private static final ThreadLocal<AccessTokenIdentifierInterface> threadLocal =
                                                          new InheritableThreadLocal<AccessTokenIdentifierInterface>();

  /**
   * Get the {@code AccessTokenIdentifier} set on the current thread.
   * @return
   */
  public static AccessTokenIdentifierInterface get() {
    return threadLocal.get();
  }

  /**
   * Set the {@code AccessTokenIdentifier} on the current thread.
   * @param accessTokenIdentifierInterface
   */
  public static void set(AccessTokenIdentifierInterface accessTokenIdentifierInterface) {
    threadLocal.set(accessTokenIdentifierInterface);
  }
}
