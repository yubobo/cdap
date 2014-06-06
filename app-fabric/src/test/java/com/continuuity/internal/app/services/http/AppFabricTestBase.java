package com.continuuity.internal.app.services.http;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.discovery.EndpointStrategy;
import com.continuuity.common.discovery.RandomEndpointStrategy;
import com.continuuity.common.discovery.TimeLimitEndpointStrategy;
import com.continuuity.common.io.Codec;
import com.continuuity.data2.datafabric.dataset.service.DatasetService;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.internal.app.services.AppFabricServer;
import com.continuuity.metrics.query.MetricsQueryService;
import com.continuuity.security.auth.AccessTokenIdentifier;
import com.continuuity.security.auth.AccessTokenIdentifierCodec;
import com.continuuity.test.internal.guice.AppFabricTestModule;
import com.google.common.collect.ObjectArrays;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * AppFabric HttpHandler Test classes can extend this class, this will allow the HttpService be setup before
 * running the handler tests, this also gives the ability to run individual test cases.
 */
public abstract class AppFabricTestBase {
  private static final String API_KEY = "SampleTestApiKey";
  private static final Header AUTH_HEADER = new BasicHeader(Constants.Gateway.CONTINUUITY_API_KEY, API_KEY);
  private static final String CLUSTER = "SampleTestClusterName";

  private static final String hostname = "127.0.0.1";

  private static int port;
  private static Injector injector;

  private static InMemoryTransactionManager txManager;
  private static AppFabricServer appFabricServer;
  private static MetricsQueryService metricsService;
  private static DatasetService dsService;
  private static TransactionSystemClient txClient;
  private static Codec<AccessTokenIdentifier> accessTokenIdentifierCodec;

  @BeforeClass
  public static void beforeClass() throws Throwable {
    CConfiguration conf = CConfiguration.create();

    conf.set(Constants.AppFabric.SERVER_ADDRESS, hostname);
    conf.set(Constants.AppFabric.OUTPUT_DIR, System.getProperty("java.io.tmpdir"));
    conf.set(Constants.AppFabric.TEMP_DIR, System.getProperty("java.io.tmpdir"));

    conf.setBoolean(Constants.Dangerous.UNRECOVERABLE_RESET, true);
    conf.setBoolean(Constants.Gateway.CONFIG_AUTHENTICATION_REQUIRED, false);
    conf.set(Constants.Gateway.CLUSTER_NAME, CLUSTER);

    injector = Guice.createInjector(new AppFabricTestModule(conf));
    txManager = injector.getInstance(InMemoryTransactionManager.class);
    txManager.startAndWait();
    dsService = injector.getInstance(DatasetService.class);
    dsService.startAndWait();
    appFabricServer = injector.getInstance(AppFabricServer.class);
    appFabricServer.startAndWait();
    DiscoveryServiceClient discoveryClient = injector.getInstance(DiscoveryServiceClient.class);
    EndpointStrategy endpointStrategy =
      new TimeLimitEndpointStrategy(
        new RandomEndpointStrategy(discoveryClient.discover(Constants.Service.APP_FABRIC_HTTP)),
        1L, TimeUnit.SECONDS);
    port = endpointStrategy.pick().getSocketAddress().getPort();
    txClient = injector.getInstance(TransactionSystemClient.class);
    metricsService = injector.getInstance(MetricsQueryService.class);
    metricsService.startAndWait();
    accessTokenIdentifierCodec = injector.getInstance(AccessTokenIdentifierCodec.class);
  }

  @AfterClass
  public static void afterClass() {
    appFabricServer.stopAndWait();
    metricsService.stopAndWait();
    dsService.stopAndWait();
    txManager.stopAndWait();
  }

  protected static Injector getInjector() {
    return injector;
  }

  protected static TransactionSystemClient getTxClient() {
    return txClient;
  }

  protected static int getPort() {
    return port;
  }

  protected static URI getEndPoint(String path) throws URISyntaxException {
    return new URI("http://" + hostname + ":" + port + path);
  }

  protected static HttpResponse doGet(String resource) throws Exception {
    return doGet(resource, null);
  }

  protected static HttpResponse doGet(String resource, Header[] headers) throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpGet get = new HttpGet(AppFabricTestBase.getEndPoint(resource));

    if (headers != null) {
      get.setHeaders(ObjectArrays.concat(AUTH_HEADER, headers));
    } else {

      get.setHeader(AUTH_HEADER);
    }
    get.setHeader(getReactorVerifiedHeader());
    return client.execute(get);
  }

  protected static HttpResponse execute(HttpUriRequest request) throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    request.setHeader(AUTH_HEADER);
    request.setHeader(getReactorVerifiedHeader());
    return client.execute(request);
  }

  protected static HttpPost getPost(String resource) throws Exception {
    HttpPost post = new HttpPost(AppFabricTestBase.getEndPoint(resource));
    post.setHeader(AUTH_HEADER);
    post.setHeader(getReactorVerifiedHeader());
    return post;
  }

  protected static HttpPut getPut(String resource) throws Exception {
    HttpPut put = new HttpPut(AppFabricTestBase.getEndPoint(resource));
    put.setHeader(AUTH_HEADER);
    put.setHeader(getReactorVerifiedHeader());
    return put;
  }

  protected static HttpResponse doPost(String resource) throws Exception {
    return doPost(resource, null, null);
  }

  protected static HttpResponse doPost(String resource, String body) throws Exception {
    return doPost(resource, body, null);
  }

  protected static HttpResponse doPost(String resource, String body, Header[] headers) throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(AppFabricTestBase.getEndPoint(resource));

    if (body != null) {
      post.setEntity(new StringEntity(body));
    }

    if (headers != null) {
      post.setHeaders(ObjectArrays.concat(AUTH_HEADER, headers));
    } else {
      post.setHeader(AUTH_HEADER);
    }
    post.setHeader(getReactorVerifiedHeader());
    return client.execute(post);
  }

  protected static HttpResponse doPost(HttpPost post) throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    post.setHeader(AUTH_HEADER);
    post.setHeader(getReactorVerifiedHeader());
    return client.execute(post);
  }

  protected static HttpResponse doPut(String resource) throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpPut put = new HttpPut(AppFabricTestBase.getEndPoint(resource));
    put.setHeader(AUTH_HEADER);
    put.setHeader(getReactorVerifiedHeader());
    return doPut(resource, null);
  }

  protected static HttpResponse doPut(String resource, String body) throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpPut put = new HttpPut(AppFabricTestBase.getEndPoint(resource));
    if (body != null) {
      put.setEntity(new StringEntity(body));
    }
    put.setHeader(AUTH_HEADER);
    put.setHeader(getReactorVerifiedHeader());
    return client.execute(put);
  }

  protected static HttpResponse doDelete(String resource) throws Exception {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpDelete delete = new HttpDelete(AppFabricTestBase.getEndPoint(resource));
    delete.setHeader(AUTH_HEADER);
    delete.setHeader(getReactorVerifiedHeader());
    return client.execute(delete);
  }

  private static BasicHeader getReactorVerifiedHeader() throws IOException {
    long time = System.currentTimeMillis();
    AccessTokenIdentifier accessTokenIdentifier = new AccessTokenIdentifier("test", Collections.<String>emptyList(), time, time + 10000000);
    byte[] encodedAccessTokenIdentifier = accessTokenIdentifierCodec.encode(accessTokenIdentifier);
    String encoded = Base64.encodeBase64String(encodedAccessTokenIdentifier).trim();
    return new BasicHeader("Authorization", Constants.Security.VERIFIED_HEADER_BASE + encoded);
  }

}
