package com.continuuity.data.stream.property;

import com.continuuity.common.io.Codec;
import com.google.common.base.Charsets;
import com.google.gson.Gson;

import java.io.IOException;

/**
 * Codec for {@link StreamTTLProperty}.
 */
public final class StreamTTLPropertyCodec implements Codec<StreamTTLProperty> {

  private static final Gson GSON = new Gson();

  @Override
  public byte[] encode(StreamTTLProperty property) throws IOException {
    return GSON.toJson(property).getBytes(Charsets.UTF_8);
  }

  @Override
  public StreamTTLProperty decode(byte[] data) throws IOException {
    return GSON.fromJson(new String(data, Charsets.UTF_8), StreamTTLProperty.class);
  }
}
