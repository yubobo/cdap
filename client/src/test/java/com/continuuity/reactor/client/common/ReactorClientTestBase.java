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

package com.continuuity.reactor.client.common;

import com.continuuity.proto.ProgramRecord;
import com.continuuity.proto.ProgramType;
import com.continuuity.reactor.client.ReactorProgramClient;
import com.continuuity.reactor.client.exception.NotFoundException;
import com.continuuity.reactor.client.exception.ProgramNotFoundException;
import org.junit.Assert;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public abstract class ReactorClientTestBase extends SingleNodeTestBase {

  protected void verifyProgramNames(List<String> expected, List<ProgramRecord> actual) {
    Assert.assertEquals(expected.size(), actual.size());
    for (ProgramRecord actualProgramRecord : actual) {
      Assert.assertTrue(expected.contains(actualProgramRecord.getId()));
    }
  }

  protected void assertProcedureInstances(ReactorProgramClient programClient, String appId, String procedureId,
                                          int numInstances) throws IOException, NotFoundException {
    int actualInstances;
    int numTries = 0;
    int maxTries = 5;
    do {
      actualInstances = programClient.getProcedureInstances(appId, procedureId);
      numTries++;
    } while (actualInstances != numInstances && numTries <= maxTries);
    Assert.assertEquals(numInstances, actualInstances);
  }

  protected void assertFlowletInstances(ReactorProgramClient programClient, String appId, String flowId,
                                        String flowletId, int numInstances) throws IOException, NotFoundException {
    int actualInstances;
    int numTries = 0;
    int maxTries = 5;
    do {
      actualInstances = programClient.getFlowletInstances(appId, flowId, flowletId);
      numTries++;
    } while (actualInstances != numInstances && numTries <= maxTries);
    Assert.assertEquals(numInstances, actualInstances);
  }

  protected void assertProgramRunning(ReactorProgramClient programClient, String appId, ProgramType programType,
                                      String programId) throws IOException, ProgramNotFoundException {
    assertProgramStatus(programClient, appId, programType, programId, "RUNNING");
  }


  protected void assertProgramStopped(ReactorProgramClient programClient, String appId, ProgramType programType,
                                      String programId) throws IOException, ProgramNotFoundException {
    assertProgramStatus(programClient, appId, programType, programId, "STOPPED");
  }

  protected void assertProgramStatus(ReactorProgramClient programClient, String appId, ProgramType programType,
                                     String programId, String programStatus)
    throws IOException, ProgramNotFoundException {

    String status;
    int numTries = 0;
    int maxTries = 5;
    do {
      status = programClient.getStatus(appId, programType, programId);
      numTries++;
    } while (!status.equals(programStatus) && numTries <= maxTries);
    Assert.assertEquals(programStatus, status);
  }

}
