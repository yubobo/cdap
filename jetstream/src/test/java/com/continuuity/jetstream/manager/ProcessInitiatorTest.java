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

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * ProcessInitiatorTest
 */

public class ProcessInitiatorTest {

  protected static HubDataStore hubDataStore;
  protected static ProcessInitiator processInitiator;

  @BeforeClass
  public static void setup() throws Exception {
    hubDataStore = new HubDataStore();
    hubDataStore = new HubDataStore();
    hubDataStore.addDataSource("source1", new InetSocketAddress("127.0.0.1",8081));
    hubDataStore.addDataSource("source2", new InetSocketAddress("127.0.0.1",8082));
    hubDataStore.addDataSink("sink1", "query1", new InetSocketAddress("127.0.0.1",8081));
    hubDataStore.addDataSink("sink2", "query2", new InetSocketAddress("127.0.0.1",8082));
    hubDataStore.setHFTACount(5);
    hubDataStore.setInstanceName("test");
    hubDataStore.setClearingHouseAddress(new InetSocketAddress("127.0.0.1",1111));
    hubDataStore.setHubAddress(new InetSocketAddress("127.0.0.1",2222));
    processInitiator = new ProcessInitiator(hubDataStore);
  }

  /*
  @Test
  public void testProcessExecution() throws IOException, InterruptedException {
    String[] arg = {"-l"};
    System.out.print(processInitiator.startProcess("ls", arg));
  }
  */

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
