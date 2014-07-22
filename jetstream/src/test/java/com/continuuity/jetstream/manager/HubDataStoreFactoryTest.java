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

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * HubDataStoreFactoryTest
 */

public class HubDataStoreFactoryTest {
  private static HubDataStore hubDataStore;

  @BeforeClass
  public static void setup() throws Exception {
    hubDataStore = new HubDataStore(null, false, null, null, 0, null, null);
  }

  @Test
  public void testSetHubAddress() {
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8080),
                        HubDataStoreFactory.setHubAddress(hubDataStore,
                                                          new InetSocketAddress("127.0.0.1",8080)).getHubAddress());
  }

  @Test
  public void testSetClearingHouseAddress() {
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8080),
                        HubDataStoreFactory.setClearingHouseAddress(hubDataStore,
                                                                    new InetSocketAddress("127.0.0.1",8080))
                                                                    .getClearingHouseAddress());
  }

  @Test
  public void testSetInstanceName() {
    Assert.assertEquals("test", HubDataStoreFactory.setInstanceName(hubDataStore, "test").getInstanceName());
  }

  @Test
  public void testInitialize() {
    Assert.assertEquals(true, HubDataStoreFactory.initialize(hubDataStore).isInitialized());
  }

  @Test
  public void testSetHFTACount() {
    Assert.assertEquals(10, HubDataStoreFactory.setHFTACount(hubDataStore, 10).getHFTACount());
  }

  @Test
  public void testAddDataSource() {
    List<HubDataSource> sourceList = Lists.newArrayList();
    sourceList.add(new HubDataSource("source1", new InetSocketAddress("127.0.0.1",8081)));
    HubDataStore tmpStore = HubDataStoreFactory.addDataSource(hubDataStore, sourceList);
    Assert.assertEquals(1, tmpStore.getHubDataSources().size());
    Assert.assertEquals("source1", tmpStore.getHubDataSources().get(0).getName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8081), tmpStore.getHubDataSources().get(0).getAddress());
    sourceList = Lists.newArrayList();
    sourceList.add(new HubDataSource("source2", new InetSocketAddress("127.0.0.1",8082)));
    sourceList.add(new HubDataSource("source3", new InetSocketAddress("127.0.0.1",8083)));
    tmpStore = HubDataStoreFactory.addDataSource(tmpStore, sourceList);
    Assert.assertEquals(3, tmpStore.getHubDataSources().size());
    Assert.assertEquals("source1", tmpStore.getHubDataSources().get(0).getName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8081), tmpStore.getHubDataSources().get(0).getAddress());
    Assert.assertEquals("source2", tmpStore.getHubDataSources().get(1).getName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8082), tmpStore.getHubDataSources().get(1).getAddress());
    Assert.assertEquals("source3", tmpStore.getHubDataSources().get(2).getName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8083), tmpStore.getHubDataSources().get(2).getAddress());
  }

  @Test
  public void testAddDataSink() {
    List<HubDataSink> sinkList = Lists.newArrayList();
    sinkList.add(new HubDataSink("sink1", "name1",new InetSocketAddress("127.0.0.1",8081)));
    HubDataStore tmpStore = HubDataStoreFactory.addDataSink(hubDataStore, sinkList);
    Assert.assertEquals(1, tmpStore.getHubDataSinks().size());
    Assert.assertEquals("sink1", tmpStore.getHubDataSinks().get(0).getName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8081), tmpStore.getHubDataSinks().get(0).getAddress());
    sinkList = Lists.newArrayList();
    sinkList.add(new HubDataSink("sink2", "name2", new InetSocketAddress("127.0.0.1",8082)));
    sinkList.add(new HubDataSink("sink3", "name3", new InetSocketAddress("127.0.0.1",8083)));
    tmpStore = HubDataStoreFactory.addDataSink(tmpStore, sinkList);
    Assert.assertEquals(3, tmpStore.getHubDataSinks().size());
    Assert.assertEquals("sink1", tmpStore.getHubDataSinks().get(0).getName());
    Assert.assertEquals("name1", tmpStore.getHubDataSinks().get(0).getFTAName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8081), tmpStore.getHubDataSinks().get(0).getAddress());
    Assert.assertEquals("sink2", tmpStore.getHubDataSinks().get(1).getName());
    Assert.assertEquals("name2", tmpStore.getHubDataSinks().get(1).getFTAName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8082), tmpStore.getHubDataSinks().get(1).getAddress());
    Assert.assertEquals("sink3", tmpStore.getHubDataSinks().get(2).getName());
    Assert.assertEquals("name3", tmpStore.getHubDataSinks().get(2).getFTAName());
    Assert.assertEquals(new InetSocketAddress("127.0.0.1",8083), tmpStore.getHubDataSinks().get(2).getAddress());
  }

}
