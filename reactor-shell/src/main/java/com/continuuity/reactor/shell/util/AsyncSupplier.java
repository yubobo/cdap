/*
 * Copyright 2012-2014 Continuuity, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.continuuity.reactor.shell.util;

import com.google.common.base.Supplier;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @param <T>
 */
public class AsyncSupplier<T> implements Supplier<T> {

  private static final ThreadFactory THREAD_FACTORY = Executors.defaultThreadFactory();

  private final Supplier<T> supplier;
  private final ThreadFactory threadFactory;

  private T previousValue;

  public AsyncSupplier(Supplier<T> supplier, ThreadFactory threadFactory) {
    this.supplier = supplier;
    this.threadFactory = threadFactory;
  }

  @Override
  public T get() {
    if (previousValue == null) {
      previousValue = supplier.get();
      return previousValue;
    }

    threadFactory.newThread(new Runnable() {
      @Override
      public void run() {
        previousValue = supplier.get();
      }
    });
    return previousValue;
  }

  public static <T> AsyncSupplier<T> of(Supplier<T> supplier) {
    return new AsyncSupplier<T>(supplier, THREAD_FACTORY);
  }
}
