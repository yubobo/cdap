package com.continuuity.data.stream.property;

import com.continuuity.common.io.Codec;
import com.google.common.base.Charsets;
import com.google.gson.Gson;

import java.io.IOException;

/**
 * Codec for {@link StreamGenerationProperty}.
 */
public final class StreamGenerationPropertyCodec implements Codec<StreamGenerationProperty> {

  private static final Gson GSON = new Gson();

  @Override
  public byte[] encode(StreamGenerationProperty property) throws IOException {
    return GSON.toJson(property).getBytes(Charsets.UTF_8);
  }

  @Override
  public StreamGenerationProperty decode(byte[] data) throws IOException {
    return GSON.fromJson(new String(data, Charsets.UTF_8), StreamGenerationProperty.class);
  }
}
