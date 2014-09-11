/*
 * Copyright 2014 Cask Data, Inc.
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

package co.cask.cdap.internal.app.store;

import co.cask.cdap.data2.dataset2.AbstractDatasetTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AppMetadataStoreTest extends AbstractDatasetTest {
  private static AppMetadataStore store;

  public AppMetadataStoreTest() {
    this.store = AppMetadataStore();
  }

  @Test
  public void testApplicationRuntimeArgs() {
    int exception = 0;
    String account_id = "1";
    String app_id = "2";
    ProgramArgs result;

    Map<String, String> args = new HashMap<String, String>();
    args.put("debug", "true");
    args.put("timeout", "3000");
    args.put("log", "false");

    // APPLICATION RUN-TIME ARGS CRUD TEST
    // write
    store.writeApplicationArgs(account_id, app_id, args);

    // read
    result = store.getApplicationArgs(account_id, app_id);
    Assert.assertEquals(result.getArgs().get("debug"), args.get("debug"));
    Assert.assertEquals(result.getArgs().get("timeout"), args.get("timeout"));
    Assert.assertEquals(result.getArgs().get("log"), args.get("log"));

    // update
    args.put("debug", "false");
    store.updateApplicationArgs(account_id, app_id, args);
    result = store.getApplicationArgs(account_id, app_id);
    Assert.assertEquals(result.getArgs().get("debug"), args.get("debug"));

    // remove
    store.deleteApplicationArgs(account_id, app_id);
    try {
      store.getApplicationArgs(account_id, app_id);
    } catch (Exception e) {
      exception = 1;
    }
    Assert.assertEquals(exception, 1);
  }

  @Test
  public void testProgramRuntimeArgs() {
    int exception = 0;
    String account_id = "1";
    String app_id = "2";
    String prog_id = "3";
    ProgramArgs result;

    Map<String, String> args = new HashMap<String, String>();
    args.put("debug", "true");
    args.put("timeout", "3000");
    args.put("log", "false");

    // PROGRAM RUN-TIME ARGS CRUD TEST
    // write
    store.writeProgramArgs(account_id, app_id, prog_id, args);

    // read
    result = store.getProgramArgs(account_id, app_id, prog_id);
    Assert.assertEquals(result.getArgs().get("debug"), args.get("debug"));
    Assert.assertEquals(result.getArgs().get("timeout"), args.get("timeout"));
    Assert.assertEquals(result.getArgs().get("log"), args.get("log"));

    // update
    args.put("debug", "false");
    store.updateProgramArgs(account_id, app_id, prog_id, args);

    result = store.getProgramArgs(account_id, app_id, prog_id);
    Assert.assertEquals(result.getArgs().get("debug"), args.get("debug"));

    // remove
    store.deleteProgramArgs(account_id, app_id);
    try {
      store.getProgramArgs(account_id, app_id, prog_id);
    } catch (Exception e) {
      exception = 1;
    }
    Assert.assertEquals(exception, 1);
  }

//  @Test
//  public void testFlowRuntimeArgs() {
//    int exception = 0;
//    String account_id = "1";
//    String app_id = "2";
//    String prog_id = "3";
//    ProgramArgs result;
//
//    Map<String, String> args = new HashMap<String, String>();
//    args.put("debug", "true");
//    args.put("timeout", "3000");
//    args.put("log", "false");
//
//    // PROGRAM RUN-TIME ARGS CRUD TEST
//    // write
//    store.writeProgramArgs(account_id, app_id, prog_id, args);
//
//    // read
//    result = store.getProgramArgs(account_id, app_id, prog_id);
//    Assert.assertEquals(result.getArgs().get("debug"), args.get("debug"));
//    Assert.assertEquals(result.getArgs().get("timeout"), args.get("timeout"));
//    Assert.assertEquals(result.getArgs().get("log"), args.get("log"));
//
//    // update
//    args.put("debug", "false");
//    store.updateProgramArgs(account_id, app_id, prog_id, args);
//
//    result = store.getProgramArgs(account_id, app_id, prog_id);
//    Assert.assertEquals(result.getArgs().get("debug"), args.get("debug"));
//
//    // remove
//    store.deleteProgramArgs(account_id, app_id);
//    try {
//      store.getProgramArgs(account_id, app_id, prog_id);
//    } catch (Exception e) {
//      exception = 1;
//    }
//    Assert.assertEquals(exception, 1);
//  }
//
//  @Test
//  public void testFlowletRuntimeArgs() {
//    int exception = 0;
//    String account_id = "1";
//    String app_id = "2";
//    String prog_id = "3";
//    ProgramArgs result;
//
//    Map<String, String> args = new HashMap<String, String>();
//    args.put("debug", "true");
//    args.put("timeout", "3000");
//    args.put("log", "false");
//
//    // PROGRAM RUN-TIME ARGS CRUD TEST
//    // write
//    store.writeProgramArgs(account_id, app_id, prog_id, args);
//
//    // read
//    result = store.getProgramArgs(account_id, app_id, prog_id);
//    Assert.assertEquals(result.getArgs().get("debug"), args.get("debug"));
//    Assert.assertEquals(result.getArgs().get("timeout"), args.get("timeout"));
//    Assert.assertEquals(result.getArgs().get("log"), args.get("log"));
//
//    // update
//    args.put("debug", "false");
//    store.updateProgramArgs(account_id, app_id, prog_id, args);
//
//    result = store.getProgramArgs(account_id, app_id, prog_id);
//    Assert.assertEquals(result.getArgs().get("debug"), args.get("debug"));
//
//    // remove
//    store.deleteProgramArgs(account_id, app_id);
//    try {
//      store.getProgramArgs(account_id, app_id, prog_id);
//    } catch (Exception e) {
//      exception = 1;
//    }
//    Assert.assertEquals(exception, 1);
//  }

}
