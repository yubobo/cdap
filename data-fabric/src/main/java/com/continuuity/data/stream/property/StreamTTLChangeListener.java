package com.continuuity.data.stream.property;

import com.continuuity.common.conf.PropertyChangeListener;
import com.continuuity.data.stream.StreamPropertyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link PropertyChangeListener} that convert onChange callback into {@link StreamPropertyListener}
 * for stream TTL changes.
 */
public final class StreamTTLChangeListener extends StreamPropertyListener
  implements PropertyChangeListener<StreamTTLProperty> {

  private static final Logger LOG = LoggerFactory.getLogger(StreamTTLChangeListener.class);

  private final StreamPropertyListener listener;

  public StreamTTLChangeListener(StreamPropertyListener listener) {
    this.listener = listener;
  }

  @Override
  public void onChange(String name, StreamTTLProperty property) {
    ttlChanged(name, property.getTTL());
  }

  @Override
  public void onError(String name, Throwable failureCause) {
    LOG.error("Exception on PropertyChangeListener for stream {}", name, failureCause);
  }

  @Override
  public void ttlChanged(String streamName, long ttl) {
    try {
      listener.ttlChanged(streamName, ttl);
    } catch (Throwable t) {
      LOG.error("Exception while calling StreamPropertyListener.ttlChanged()", t);
    }
  }
}
