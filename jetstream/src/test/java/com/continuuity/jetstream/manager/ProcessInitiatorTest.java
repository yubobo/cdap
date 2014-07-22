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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * ProcessInitiatorTest
 */

public class ProcessInitiatorTest {

  private static HubDataStore hubDataStore;
  private static ProcessInitiator processInitiator;

  @BeforeClass
  public static void setup() throws Exception {
    List<HubDataSource> sourceList = Lists.newArrayList();
    sourceList.add(new HubDataSource("source1", new InetSocketAddress("127.0.0.1",8081)));
    sourceList.add(new HubDataSource("source2", new InetSocketAddress("127.0.0.1",8082)));
    List<HubDataSink> sinkList = Lists.newArrayList();
    sinkList.add(new HubDataSink("sink1", "query1", new InetSocketAddress("127.0.0.1",7081)));
    sinkList.add(new HubDataSink("sink2", "query2", new InetSocketAddress("127.0.0.1", 7082)));
    hubDataStore = new HubDataStore("test", false, sourceList, sinkList, 5, new InetSocketAddress("127.0.0.1",2222),
                                    new InetSocketAddress("127.0.0.1",1111));
    processInitiator = new ProcessInitiator(hubDataStore);
  }

/**
 *  These Tests Return IOExceptions as these binaries don't exist
 */

  @Test
  public void testRTSExecution() throws IOException, InterruptedException {
    try {
      processInitiator.startRTS();
    } catch (IOException e) {
      Assert.assertEquals("java.io.IOException: Cannot run program \"rts\": error=2, No such file or directory",
                          e.toString());
    }
  }

  @Test
  public void testHFTAExecution() throws IOException, InterruptedException {
    try {
      processInitiator.startHFTA();
    } catch (IOException e) {
      Assert.assertEquals("java.io.IOException: Cannot run program \"hfta_0\": error=2, No such file or directory",
                          e.toString());
    }
  }

  @Test
  public void testGSEXITExecution() throws IOException, InterruptedException {
    try {
      processInitiator.startGSEXIT();
    } catch (IOException e) {
      Assert.assertEquals("java.io.IOException: Cannot run program \"GSEXIT\": error=2, No such file or directory",
                          e.toString());
    }
  }

}
