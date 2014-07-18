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
import com.google.gson.JsonArray;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * DataStoreTest
 */

public class DataStoreTest {
  private static DataStore dataStore;

  @BeforeClass
  public static void setup() throws Exception {
    dataStore = new DataStore();
  }

  @Test
  public void testDataSources() {
    dataStore.addDataSource("source1", "127.0.0.1:8081");
    JsonArray dataSources = dataStore.getDataSources();
    Assert.assertEquals("source1", dataSources.get(0).getAsJsonObject().get("name").getAsString());
    Assert.assertEquals("127.0.0.1:8081", dataSources.get(0).getAsJsonObject().get("address").getAsString());
    dataStore.addDataSource("source2", "127.0.0.1:8082");
    dataSources = dataStore.getDataSources();
    Assert.assertEquals("source1", dataSources.get(0).getAsJsonObject().get("name").getAsString());
    Assert.assertEquals("127.0.0.1:8081", dataSources.get(0).getAsJsonObject().get("address").getAsString());
    Assert.assertEquals("source2", dataSources.get(1).getAsJsonObject().get("name").getAsString());
    Assert.assertEquals("127.0.0.1:8082", dataSources.get(1).getAsJsonObject().get("address").getAsString());
  }

  @Test
  public void testDataSinks() {
    dataStore.addDataSink("sink1", "query1", "127.0.0.1:8081");
    JsonArray dataSources = dataStore.getDataSinks();
    Assert.assertEquals("sink1", dataSources.get(0).getAsJsonObject().get("name").getAsString());
    Assert.assertEquals("query1", dataSources.get(0).getAsJsonObject().get("fta_name").getAsString());
    Assert.assertEquals("127.0.0.1:8081", dataSources.get(0).getAsJsonObject().get("address").getAsString());
    dataStore.addDataSink("sink2", "query2", "127.0.0.1:8082");
    dataSources = dataStore.getDataSinks();
    Assert.assertEquals("sink1", dataSources.get(0).getAsJsonObject().get("name").getAsString());
    Assert.assertEquals("query1", dataSources.get(0).getAsJsonObject().get("fta_name").getAsString());
    Assert.assertEquals("127.0.0.1:8081", dataSources.get(0).getAsJsonObject().get("address").getAsString());
    Assert.assertEquals("sink2", dataSources.get(1).getAsJsonObject().get("name").getAsString());
    Assert.assertEquals("query2", dataSources.get(1).getAsJsonObject().get("fta_name").getAsString());
    Assert.assertEquals("127.0.0.1:8082", dataSources.get(1).getAsJsonObject().get("address").getAsString());
  }

  @Test
  public void testHFTACount() {
    dataStore.setHFTACount(5);
    Assert.assertEquals(5, dataStore.getHFTACount());
  }

  @Test
  public void testHubAddress() {
    dataStore.setHubAddress("127.0.0.1:8080");
    Assert.assertEquals("127.0.0.1:8080", dataStore.getHubAddress());
  }

  @Test
  public void testClearingHouse() {
    dataStore.setClearingHouseAddress("127.0.0.1:8080");
    Assert.assertEquals("127.0.0.1:8080", dataStore.getClearingHouseAddress());
  }

  @Test
  public void testInstanceName() {
    dataStore.setInstanceName("Test");
    Assert.assertEquals("Test", dataStore.getInstanceName());
    dataStore.setInstanceName("Test2");
    Assert.assertEquals("Test2", dataStore.getInstanceName());
  }
}
