/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.test.internal;

import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.flow.flowlet.StreamEvent;
import co.cask.cdap.common.stream.StreamEventTypeAdapter;
import co.cask.cdap.data.stream.service.StreamFetchHandler;
import co.cask.cdap.data.stream.service.StreamHandler;
import co.cask.cdap.internal.io.SchemaTypeAdapter;
import co.cask.cdap.proto.Id;
import co.cask.cdap.test.StreamManager;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link StreamManager} for use in tests
 */
public class DefaultStreamManager implements StreamManager {
  private static final Gson GSON = StreamEventTypeAdapter.register(
    new GsonBuilder().registerTypeAdapter(Schema.class, new SchemaTypeAdapter())).create();

  private final Id.Stream streamId;
  private final StreamHandler streamHandler;
  private final StreamFetchHandler streamFetchHandler;

  @Inject
  public DefaultStreamManager(StreamHandler streamHandler, StreamFetchHandler streamFetchHandler,
                              @Assisted("streamId") Id.Stream streamId) {
    this.streamHandler = streamHandler;
    this.streamFetchHandler = streamFetchHandler;
    this.streamId = streamId;
  }

  @Override
  public void createStream() throws IOException {
    String path = String.format("/v3/namespaces/%s/streams/%s", streamId.getNamespaceId(), streamId.getId());
    HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);

    MockResponder responder = new MockResponder();
    try {
      streamHandler.create(httpRequest, responder, streamId.getNamespaceId(), streamId.getId());
    } catch (Exception e) {
      Throwables.propagateIfPossible(e, IOException.class);
      throw Throwables.propagate(e);
    }
    if (responder.getStatus() != HttpResponseStatus.OK) {
      throw new IOException("Failed to create stream. Status = " + responder.getStatus());
    }
  }

  @Override
  public void send(String content) throws IOException {
    send(Charsets.UTF_8.encode(content));
  }

  @Override
  public void send(byte[] content) throws IOException {
    send(content, 0, content.length);
  }

  @Override
  public void send(byte[] content, int off, int len) throws IOException {
    send(ByteBuffer.wrap(content, off, len));
  }

  @Override
  public void send(ByteBuffer buffer) throws IOException {
    send(ImmutableMap.<String, String>of(), buffer);
  }

  @Override
  public void send(Map<String, String> headers, String content) throws IOException {
    send(headers, Charsets.UTF_8.encode(content));
  }

  @Override
  public void send(Map<String, String> headers, byte[] content) throws IOException {
    send(headers, content, 0, content.length);
  }

  @Override
  public void send(Map<String, String> headers, byte[] content, int off, int len) throws IOException {
    send(headers, ByteBuffer.wrap(content, off, len));
  }

  @Override
  public void send(Map<String, String> headers, ByteBuffer buffer) throws IOException {
    String path = String.format("/v3/namespaces/%s/streams/%s", streamId.getNamespaceId(), streamId.getId());
    HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);

    for (Map.Entry<String, String> entry : headers.entrySet()) {
      httpRequest.setHeader(streamId.getId() + "." + entry.getKey(), entry.getValue());
    }
    ChannelBuffer content = ChannelBuffers.wrappedBuffer(buffer);
    httpRequest.setContent(content);
    httpRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());

    MockResponder responder = new MockResponder();
    try {
      streamHandler.enqueue(httpRequest, responder, streamId.getNamespaceId(), streamId.getId());
    } catch (Exception e) {
      Throwables.propagateIfPossible(e, IOException.class);
      throw Throwables.propagate(e);
    }
    if (responder.getStatus() != HttpResponseStatus.OK) {
      throw new IOException("Failed to write to stream. Status = " + responder.getStatus());
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }

  @Override
  public List<StreamEvent> getEvents(long startTime, long endTime, int limit) throws IOException {
    return getEvents(streamId, startTime, endTime, limit);
  }

  private List<StreamEvent> getEvents(Id.Stream streamId, long startTime, long endTime, int limit) throws IOException {
    String path = String.format("/v3/namespaces/%s/streams/%s/events?start=%d&end=%d&limit=%d",
                                streamId.getNamespaceId(), streamId.getId(), startTime, endTime, limit);
    HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);

    MockResponder responder = new MockResponder();
    try {
      streamFetchHandler.fetch(httpRequest, responder, streamId.getNamespaceId(), streamId.getId(),
                               startTime, endTime, limit);
    } catch (Exception e) {
      Throwables.propagateIfPossible(e, IOException.class);
      throw Throwables.propagate(e);
    }
    if (responder.getStatus() != HttpResponseStatus.OK) {
      throw new IOException("Failed to read from stream. Status = " + responder.getStatus());
    }

    return responder.decodeResponseContent(new TypeToken<List<StreamEvent>>() { }, GSON);
  }
}