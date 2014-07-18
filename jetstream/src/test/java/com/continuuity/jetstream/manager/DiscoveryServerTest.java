/**
 * Copyright 2012-2014 Continuuity, Inc.
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

package com.continuuity.jetstream.manager;

import com.continuuity.jetstream.manager.DataStore;
import com.continuuity.jetstream.manager.DiscoveryServer;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.Service;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * DiscoveryServerTest
 */

public class DiscoveryServerTest {

  protected static URI baseURI;
  protected static DataStore dataStore;
  protected static DiscoveryServer discoveryServer;

  @BeforeClass
  public static void setup() throws Exception {
    dataStore = new DataStore();
    dataStore.addDataSource("source1", "127.0.0.1:8081");
    dataStore.addDataSource("source2", "127.0.0.1:8082");
    dataStore.addDataSink("sink1", "query1", "127.0.0.1:8081");
    dataStore.addDataSink("sink2", "query2", "127.0.0.1:8082");
    dataStore.setHFTACount(5);
    dataStore.setInstanceName("test");
    dataStore.setClearingHouseAddress("127.0.0.1:1111");
    discoveryServer = new DiscoveryServer(dataStore);
    baseURI = URI.create(String.format("http://" + discoveryServer.getHubAddress()));
  }

  protected HttpURLConnection request(String path, HttpMethod method) throws IOException {
    URL url = baseURI.resolve(path).toURL();
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
    if (method == HttpMethod.POST || method == HttpMethod.PUT) {
      urlConn.setDoOutput(true);
    }
    urlConn.setRequestMethod(method.getName());
    urlConn.setRequestProperty(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
    return urlConn;
  }

  protected String getContent(HttpURLConnection urlConn) throws IOException {
    return new String(ByteStreams.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
  }

  protected void writeContent(HttpURLConnection urlConn, String content) throws IOException {
    urlConn.getOutputStream().write(content.getBytes(Charsets.UTF_8));
  }

  @Test
  public void testServiceState() throws IOException {
    Assert.assertEquals(Service.State.RUNNING, discoveryServer.state());
  }

  @Test
  public void testDiscoverInstance() throws IOException {
    HttpURLConnection urlConn = request("/v1/DiscoverInstance/TestInstance", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testDiscoverInitializedInstance() throws IOException {
    HttpURLConnection urlConn = request("/v1/DiscoverInitializedInstance/TestInstance", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testAnnounceInstance() throws IOException {
    HttpURLConnection urlConn = request("/v1/AnnounceInstance", HttpMethod.POST);
    writeContent(urlConn, "{\"name\":\"TestInstance\",\"ip\":\"127.0.0.1\",\"port\":\"9999\"}");
    urlConn.disconnect();
    Assert.assertEquals(200, urlConn.getResponseCode());
    Assert.assertEquals("TestInstance", discoveryServer.getInstanceName());
    Assert.assertEquals("127.0.0.1:9999", discoveryServer.getClearingHouseAddress());
  }

  @Test
  public void testAnnounceInitializedInstance() throws IOException {
    HttpURLConnection urlConn = request("/v1/AnnounceInitializedInstance", HttpMethod.POST);
    writeContent(urlConn, "{\"name\":\"TestInstance1\"}");
    Assert.assertEquals(200, urlConn.getResponseCode());
    Assert.assertEquals("TestInstance1", discoveryServer.getInstanceName());
  }

  @Test
  public void testDataSource() throws IOException {
    HttpURLConnection urlConn = request("/v1/DiscoverSource/source1", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
  }

  @Test
  public void testBadDataSource() throws IOException {
    HttpURLConnection urlConn = request("/v1/DiscoverSource/badSource", HttpMethod.GET);
    Assert.assertEquals(400, urlConn.getResponseCode());
  }

  @Test
  public void testDataSink() throws IOException {
    HttpURLConnection urlConn = request("/v1/DiscoverSink/sink1", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
  }

  @Test
  public void testBadDataSink() throws IOException {
    HttpURLConnection urlConn = request("/v1/DiscoverSink/badSink", HttpMethod.GET);
    Assert.assertEquals(400, urlConn.getResponseCode());
  }
}
