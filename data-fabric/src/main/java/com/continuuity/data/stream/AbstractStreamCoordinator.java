/*
 * Copyright 2014 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data.stream;

import com.continuuity.common.async.ExecutorUtils;
import com.continuuity.common.conf.PropertyChangeListener;
import com.continuuity.common.conf.PropertyStore;
import com.continuuity.common.conf.PropertyUpdater;
import com.continuuity.common.io.Codec;
import com.continuuity.common.io.Locations;
import com.continuuity.data.stream.property.StreamGenerationChangeListener;
import com.continuuity.data.stream.property.StreamGenerationProperty;
import com.continuuity.data.stream.property.StreamGenerationPropertyCodec;
import com.continuuity.data.stream.property.StreamTTLChangeListener;
import com.continuuity.data.stream.property.StreamTTLProperty;
import com.continuuity.data.stream.property.StreamTTLPropertyCodec;
import com.continuuity.data2.transaction.stream.StreamConfig;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;
import org.apache.twill.common.Cancellable;
import org.apache.twill.common.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/**
 * Base implementation for {@link StreamCoordinator}.
 */
public abstract class AbstractStreamCoordinator implements StreamCoordinator {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractStreamCoordinator.class);

  // Executor for performing update action asynchronously
  private final Executor updateExecutor;
  private final Supplier<PropertyStore<StreamGenerationProperty>> generationPropertyStore;
  private final Supplier<PropertyStore<StreamTTLProperty>> ttlPropertyStore;

  protected AbstractStreamCoordinator() {
    generationPropertyStore = Suppliers.memoize(new Supplier<PropertyStore<StreamGenerationProperty>>() {
      @Override
      public PropertyStore<StreamGenerationProperty> get() {
        return createPropertyStore(new StreamGenerationPropertyCodec());
      }
    });

    ttlPropertyStore = Suppliers.memoize(new Supplier<PropertyStore<StreamTTLProperty>>() {
      @Override
      public PropertyStore<StreamTTLProperty> get() {
        return createPropertyStore(new StreamTTLPropertyCodec());
      }
    });

    // Update action should be infrequent, hence just use an executor that create a new thread everytime.
    updateExecutor = ExecutorUtils.newThreadExecutor(Threads.createDaemonThreadFactory("stream-coordinator-update-%d"));
  }

  /**
   * Creates a {@link PropertyStore}.
   *
   * @param codec Codec for the property stored in the property store
   * @param <T> Type of the property
   * @return A new {@link PropertyStore}.
   */
  protected abstract <T> PropertyStore<T> createPropertyStore(Codec<T> codec);

  @Override
  public ListenableFuture<Integer> nextGeneration(final StreamConfig streamConfig, final int lowerBound) {
    return Futures.transform(generationPropertyStore.get().update(streamConfig.getName(),
                                                                  new PropertyUpdater<StreamGenerationProperty>() {
      @Override
      public ListenableFuture<StreamGenerationProperty> apply(@Nullable final StreamGenerationProperty property) {
        final SettableFuture<StreamGenerationProperty> resultFuture = SettableFuture.create();
        updateExecutor.execute(new Runnable() {

          @Override
          public void run() {
            try {
              int newGeneration = ((property == null) ? lowerBound : property.getGeneration()) + 1;
              // Create the generation directory
              Locations.mkdirsIfNotExists(
                StreamUtils.createGenerationLocation(streamConfig.getLocation(), newGeneration));
              resultFuture.set(new StreamGenerationProperty(newGeneration));
            } catch (IOException e) {
              resultFuture.setException(e);
            }
          }
        });
        return resultFuture;
      }
    }), new Function<StreamGenerationProperty, Integer>() {
      @Override
      public Integer apply(StreamGenerationProperty property) {
        return property.getGeneration();
      }
    });
  }

  @Override
  public ListenableFuture<Long> changeTTL(final StreamConfig streamConfig, final long newTTL) {
    return Futures.transform(ttlPropertyStore.get().update(streamConfig.getName(),
                                                           new PropertyUpdater<StreamTTLProperty>() {
      @Override
      public ListenableFuture<StreamTTLProperty> apply(@Nullable final StreamTTLProperty property) {
        final SettableFuture<StreamTTLProperty> resultFuture = SettableFuture.create();
        updateExecutor.execute(new Runnable() {
          @Override
          public void run() {
            resultFuture.set(new StreamTTLProperty(newTTL));
          }
        });
        return resultFuture;
      }
    }), new Function<StreamTTLProperty, Long>() {
      @Override
      public Long apply(StreamTTLProperty property) {
        return property.getTTL();
      }
    });
  }

  @Override
  public Collection<Cancellable> addListener(String streamName, StreamPropertyListener listener) {
    return ImmutableList.of(
      generationPropertyStore.get().addChangeListener(streamName, new StreamGenerationChangeListener(listener)),
      ttlPropertyStore.get().addChangeListener(streamName, new StreamTTLChangeListener(listener)));
  }

  @Override
  public void close() throws IOException {
    generationPropertyStore.get().close();
    ttlPropertyStore.get().close();
  }

  /**
   * Object for holding property value in the property store.
   */
  private static final class StreamProperty {

    private final int generation;

    private StreamProperty(int generation) {
      this.generation = generation;
    }

    public int getGeneration() {
      return generation;
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
        .add("generation", generation)
        .toString();
    }
  }

}
