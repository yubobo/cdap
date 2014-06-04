package com.continuuity.data.stream.property;

/**
 * Object for holding property value in the property store.
 */
public final class StreamGenerationProperty {

  private final int generation;

  public StreamGenerationProperty(int generation) {
    this.generation = generation;
  }

  public int getGeneration() {
    return generation;
  }
}
