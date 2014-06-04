/*
 * Copyright 2014 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data.stream;

import com.continuuity.data2.transaction.stream.StreamConfig;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.twill.common.Cancellable;

import java.io.Closeable;
import java.util.Collection;

/**
 * This class responsible for process coordination needed between stream writers and consumers.
 */
public interface StreamCoordinator extends Closeable {

  /**
   * Increments the generation of the given stream.
   *
   * @param streamConfig stream configuration
   * @param lowerBound The minimum generation id to increment from. It is guaranteed that the resulting generation
   *                   would be greater than this lower bound value.
   * @return A future that will be completed when the update of generation is done. The future result will carry
   *         the generation id updated by this method.
   */
  ListenableFuture<Integer> nextGeneration(StreamConfig streamConfig, int lowerBound);

  /**
   * Changes the TTL of the given stream.
   *
   * @param streamConfig stream configuration
   * @param newTTL The desired new TTL.
   * @return A future that will be completed when the update of TTL is done. The future result will
   *         carry the TTL updated by this method.
   */
  ListenableFuture<Long> changeTTL(StreamConfig streamConfig, long newTTL);

  /**
   * Receives event for changes in stream properties.
   *
   * @param listener listener to get called when there is change in stream properties.
   * @return A {@link Collection of Cancellable} to cancel the watch
   */
  // TODO: consider return single Cancellable that cancels multiple Cancellables
  Collection<Cancellable> addListener(String streamName, StreamPropertyListener listener);
}
