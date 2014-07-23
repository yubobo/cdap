/*
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

package com.continuuity.reactor.client;

import com.continuuity.client.ApplicationClient;
import com.continuuity.client.config.ReactorClientConfig;
import com.continuuity.proto.ProgramRecord;
import com.continuuity.proto.ProgramType;
import com.continuuity.reactor.client.app.FakeApp;
import com.continuuity.reactor.client.common.ClientTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class ApplicationClientTest extends ClientTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationClientTest.class);

  private ApplicationClient appClient;

  @Before
  public void setUp() throws Throwable {
    super.setUp();

    appClient = new ApplicationClient(new ReactorClientConfig("localhost"));
  }

  @Test
  public void testAll() throws Exception {
    Assert.assertEquals(0, appClient.list().size());

    // deploy app
    LOG.info("Deploying app");
    appClient.deploy(createAppJarFile(FakeApp.class));
    Assert.assertEquals(1, appClient.list().size());

    // check program list
    LOG.info("Checking program list for app");
    Map<ProgramType, List<ProgramRecord>> programs = appClient.listPrograms(FakeApp.NAME);
    verifyProgramNames(FakeApp.FLOWS, programs.get(ProgramType.FLOW));
    verifyProgramNames(FakeApp.PROCEDURES, programs.get(ProgramType.PROCEDURE));
    verifyProgramNames(FakeApp.MAPREDUCES, programs.get(ProgramType.MAPREDUCE));
    verifyProgramNames(FakeApp.WORKFLOWS, programs.get(ProgramType.WORKFLOW));
    verifyProgramNames(FakeApp.SERVICES, programs.get(ProgramType.SERVICE));

    // delete app
    LOG.info("Deleting app");
    appClient.delete(FakeApp.NAME);
    Assert.assertEquals(0, appClient.list().size());
  }
}
