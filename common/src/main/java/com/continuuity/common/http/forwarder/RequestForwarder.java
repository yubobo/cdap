package com.continuuity.common.http.forwarder;

import com.continuuity.common.http.core.HttpResponder;
import com.google.common.collect.ImmutableListMultimap;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Forwards HTTP requests to another server.
 */
public class RequestForwarder implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(RequestForwarder.class);
  private final AsyncHttpClient asyncHttpClient;

  public RequestForwarder() {
    AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
    this.asyncHttpClient = new AsyncHttpClient(new NettyAsyncHttpProvider(configBuilder.build()),
                                               configBuilder.build());
  }

  public RequestForwarder(AsyncHttpClientConfig config) {
    this.asyncHttpClient = new AsyncHttpClient(new NettyAsyncHttpProvider(config), config);
  }

  public void forward(final HttpRequest request, final HttpResponder responder) {
    try {
      // Construct request
      RequestBuilder requestBuilder = new RequestBuilder(request.getMethod().getName());
      requestBuilder.setUrl(request.getUri());

      // Copy input
      ChannelBuffer content = request.getContent();
      if (content.readable()) {
        requestBuilder.setBody(new ChannelBufferEntityWriter(content), content.readableBytes());
      }

      // Add headers
      if (request.getHeaders() != null) {
        for (Map.Entry<String, String> entry : request.getHeaders()) {
          requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
      }

      asyncHttpClient.executeRequest(requestBuilder.build(), new AsyncCompletionHandler<Void>() {
        @Override
        public Void onCompleted(Response response) throws Exception {
          if (response.getStatusCode() == HttpResponseStatus.OK.getCode()) {
            String contentType = response.getContentType();
            ChannelBuffer content = getResponseBody(response);

            // Copy headers
            ImmutableListMultimap.Builder<String, String> headerBuilder = ImmutableListMultimap.builder();
            if (response.getHeaders() != null) {
              for (Map.Entry<String, List<String>> entry : response.getHeaders()) {
                headerBuilder.putAll(entry.getKey(), entry.getValue());
              }
            }

            responder.sendContent(HttpResponseStatus.OK, content, contentType, headerBuilder.build());
          } else {
            responder.sendStatus(HttpResponseStatus.valueOf(response.getStatusCode()));
          }
          return null;
        }

        @Override
        public void onThrowable(Throwable t) {
          LOG.error("Got exception while forwarding to uri {}", request.getUri(), t);
          responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
      });
    } catch (Throwable t) {
      LOG.error("Got exception while forwarding to uri {}", request.getUri(), t);
    }
  }

  @Override
  public void close() {
    asyncHttpClient.close();
  }

  private ChannelBuffer getResponseBody(Response response) throws IOException {
    // Optimization for NettyAsyncHttpProvider
    if (response instanceof NettyResponse) {
      // This avoids copying
      return ((NettyResponse) response).getResponseBodyAsChannelBuffer();
    }
    // This may copy, depending on the Response implementation.
    return ChannelBuffers.wrappedBuffer(response.getResponseBodyAsByteBuffer());
  }
}
