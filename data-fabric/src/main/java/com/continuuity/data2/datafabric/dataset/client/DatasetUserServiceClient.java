package com.continuuity.data2.datafabric.dataset.client;

import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.apache.twill.discovery.ServiceDiscovered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 */
public class DatasetUserServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUserServiceClient.class);
  private static final Gson GSON = new Gson();
  private static final long TIMEOUT = 60000;

  private final String user;
  private final InetSocketAddress userServiceAddress;

  public DatasetUserServiceClient(String user, DiscoveryServiceClient discoveryServiceClient) {
    this.user = user;

    String runnableName = "dataset.user." + user;
    LOG.info("Attempting to discover dataset user service {}", runnableName);

    long timeStartedDiscover = System.currentTimeMillis();
    ServiceDiscovered discover = discoveryServiceClient.discover(runnableName);
    while (!discover.iterator().hasNext()) {
      discover = discoveryServiceClient.discover(runnableName);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        LOG.error("Error", e);
      }

      if (System.currentTimeMillis() - timeStartedDiscover > TIMEOUT) {
        throw new RuntimeException("TIMED OUT: Couldn't discover dataset user service " + runnableName);
      }
    }

    Discoverable discoverable = discover.iterator().next();
    LOG.info("Discovered dataset user service {} at {}", runnableName, discoverable.getSocketAddress());
    this.userServiceAddress = discoverable.getSocketAddress();
  }

  public boolean exists(String name) throws IOException {
    // TODO(alvin): error handling
    return GSON.fromJson(doGet("/datasets/admin/" + name + "/exists").responseBody, Boolean.class);
  }

  private HttpResponse doGet(String url) throws IOException {
    HttpClient httpClient = new HttpClient();
    GetMethod getMethod = new GetMethod(url);
    int statusCode = httpClient.executeMethod(getMethod);
    String responseBody = getMethod.getResponseBodyAsString();
    return new HttpResponse(statusCode, responseBody);
  }

  private static final class HttpResponse {
    private int statusCode;
    private String responseBody;

    private HttpResponse(int statusCode, String responseBody) {
      this.statusCode = statusCode;
      this.responseBody = responseBody;
    }
  }
}
