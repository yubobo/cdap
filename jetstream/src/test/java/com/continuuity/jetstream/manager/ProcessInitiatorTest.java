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
import com.continuuity.jetstream.manager.ProcessInitiator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * ProcessInitiatorTest
 */

public class ProcessInitiatorTest {

  protected static DataStore dataStore;
  protected static ProcessInitiator processInitiator;

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
    dataStore.setHubAddress("testAddress");
    processInitiator = new ProcessInitiator(dataStore);
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
      System.out.print(processInitiator.startRTS());
    } catch (IOException e) {
      //System.out.print(e.toString());
      Assert.assertEquals("java.io.IOException: Cannot run program \"rts\": error=2, No such file or directory",
                          e.toString());
    }
  }

  @Test
  public void testHFTAExecution() throws IOException, InterruptedException {
    try {
      System.out.print(processInitiator.startHFTA());
    } catch (IOException e) {
      Assert.assertEquals("java.io.IOException: Cannot run program \"hfta_0\": error=2, No such file or directory",
                          e.toString());
    }
  }

  @Test
  public void testGSEXITExecution() throws IOException, InterruptedException {
    try {
      System.out.print(processInitiator.startGSEXIT());
    } catch (IOException e) {
      Assert.assertEquals("java.io.IOException: Cannot run program \"GSEXIT\": error=2, No such file or directory",
                          e.toString());
    }
  }

}
