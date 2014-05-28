package com.continuuity.api.data.stream;

import com.continuuity.data2.transaction.stream.StreamConfig;

/**
 * Specification for {@link Stream}.
 */
public final class StreamSpecification {
  private final String name;

  private StreamSpecification(final String name) {
    this.name = name;
  }

  /**
   * Returns the name of the Stream.
   */
  public String getName() {
    return name;
  }

  /**
   * @return A {@link StreamSpecification} from a {@link StreamConfig}.
   */
  public static StreamSpecification from(StreamConfig config) {
    return new StreamSpecification(config.getName());
  }

  /**
   * {@code StreamSpecification} builder used to build specification of stream.
   */
  public static final class Builder {
    private String name;

    /**
     * Adds name parameter to Streams.
     * @param name stream name
     * @return Builder instance
     */
    public Builder setName(final String name) {
      this.name = name;
      return this;
    }

    /**
     * Create {@code StreamSpecification}.
     * @return Instance of {@code StreamSpecification}
     */
    public StreamSpecification create() {
      StreamSpecification specification = new StreamSpecification(name);
      return specification;
    }
  }
}
