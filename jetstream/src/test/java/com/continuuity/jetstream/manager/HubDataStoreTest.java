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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * HubDataStoreTest
 */

public class HubDataStoreTest {
  private static HubDataStore hubDataStore;

  @BeforeClass
  public static void setup() throws Exception {
    hubDataStore = new HubDataStore();
  }

  @Test
  public void testDataSources() {
    hubDataStore.addDataSource("source1", new InetSocketAddress("127.0.0.1",8081));
    List<HubDataSource> dataSources = hubDataStore.getHubDataSources();
    Assert.assertEquals("source1", dataSources.get(0).getName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8081), dataSources.get(0).getAddress());
    hubDataStore.addDataSource("source2", new InetSocketAddress("127.0.0.1", 8082));
    dataSources = hubDataStore.getHubDataSources();
    Assert.assertEquals("source1", dataSources.get(0).getName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8081), dataSources.get(0).getAddress());
    Assert.assertEquals("source2", dataSources.get(1).getName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8082), dataSources.get(1).getAddress());
  }

  @Test
  public void testDataSinks() {
    hubDataStore.addDataSink("sink1", "query1", new InetSocketAddress("127.0.0.1",8081));
    List<HubDataSink> dataSinks= hubDataStore.getHubDataSinks();
    Assert.assertEquals("sink1", dataSinks.get(0).getName());
    Assert.assertEquals("query1", dataSinks.get(0).getFtaName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8081), dataSinks.get(0).getAddress());
    hubDataStore.addDataSink("sink2", "query2", new InetSocketAddress("127.0.0.1", 8082));
    dataSinks = hubDataStore.getHubDataSinks();
    Assert.assertEquals("sink1", dataSinks.get(0).getName());
    Assert.assertEquals("query1", dataSinks.get(0).getFtaName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8081), dataSinks.get(0).getAddress());
    Assert.assertEquals("sink2", dataSinks.get(1).getName());
    Assert.assertEquals("query2", dataSinks.get(1).getFtaName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8082), dataSinks.get(1).getAddress());
  }

  @Test
  public void testHFTACount() {
    hubDataStore.setHFTACount(5);
    Assert.assertEquals(5, hubDataStore.getHFTACount());
  }

  @Test
  public void testHubAddress() {
    hubDataStore.setHubAddress(new InetSocketAddress("127.0.0.1",8080));
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8080), hubDataStore.getHubAddress());
  }

  @Test
  public void testClearingHouse() {
    hubDataStore.setClearingHouseAddress(new InetSocketAddress("127.0.0.1",8080));
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8080), hubDataStore.getClearingHouseAddress());
  }

  @Test
  public void testInstanceName() {
    hubDataStore.setInstanceName("Test");
    Assert.assertEquals("Test", hubDataStore.getInstanceName());
    hubDataStore.setInstanceName("Test2");
    Assert.assertEquals("Test2", hubDataStore.getInstanceName());
  }
}
