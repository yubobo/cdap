package com.continuuity.data.stream.property;

import com.continuuity.common.conf.PropertyChangeListener;
import com.continuuity.data.stream.StreamPropertyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link PropertyChangeListener} that convert onChange callback into {@link StreamPropertyListener}
 * for stream generation changes.
 */
public final class StreamGenerationChangeListener extends StreamPropertyListener
  implements PropertyChangeListener<StreamGenerationProperty> {

  private static final Logger LOG = LoggerFactory.getLogger(StreamGenerationChangeListener.class);

  private final StreamPropertyListener listener;
  // Callback from PropertyStore is
  private StreamGenerationProperty currentProperty;

  public StreamGenerationChangeListener(StreamPropertyListener listener) {
    this.listener = listener;
  }

  @Override
  public void onChange(String name, StreamGenerationProperty property) {
    try {
      if (property == null) {
        // Property is deleted
        if (currentProperty != null) {
          // Fire all delete events
          generationDeleted(name);
        }
        return;
      }

      if (currentProperty == null) {
        // Fire all events
        generationChanged(name, property.getGeneration());
        return;
      }

      // Inspect individual stream property to determine what needs to be fired
      if (currentProperty.getGeneration() < property.getGeneration()) {
        generationChanged(name, property.getGeneration());
      }
    } finally {
      currentProperty = property;
    }
  }

  @Override
  public void onError(String name, Throwable failureCause) {
    LOG.error("Exception on PropertyChangeListener for stream {}", name, failureCause);
  }

  @Override
  public void generationChanged(String streamName, int generation) {
    try {
      listener.generationChanged(streamName, generation);
    } catch (Throwable t) {
      LOG.error("Exception while calling StreamPropertyListener.generationChanged()", t);
    }
  }

  @Override
  public void generationDeleted(String streamName) {
    try {
      listener.generationDeleted(streamName);
    } catch (Throwable t) {
      LOG.error("Exception while calling StreamPropertyListener.generationDeleted()", t);
    }
  }
}
