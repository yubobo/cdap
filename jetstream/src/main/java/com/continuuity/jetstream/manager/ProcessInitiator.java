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

import com.google.gson.JsonArray;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * ProcessInitiator is responsible for initiating all processes required by GigaScope
 */

public class ProcessInitiator {
  private DataStore dataStore;
  private Runtime rt;
  private ArrayList<Process> rtsProcessList;
  private ArrayList<Process> hftaProcessList;
  private ArrayList<Process> gsExitProcessList;

  /**
   * Constructs ProcessInitiator object for the specified DataStore
   * @param ds
   */
  public ProcessInitiator(DataStore ds) {
    this.dataStore = ds;
    this.rt = Runtime.getRuntime();
    this.rtsProcessList = new ArrayList<Process>();
    this.hftaProcessList = new ArrayList<Process>();
    this.gsExitProcessList = new ArrayList<Process>();
    //this.init();
  }

  /**
   * Sequentially invokes the required GigaScope processes
   */
  public void init() {
    try {
      this.startRTS();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot initiate RTS. Missing or bad arguments");
    }
    try {
      this.startHFTA();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot initiate HFTA processes. Missing or bad arguments");
    }
    try {
      this.startGSEXIT();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot initiate GSEXIT processes. Missing or bad arguments");
    }
  }

  /**
   * Starts RTS process
   * @throws IOException
   * @throws InterruptedException
   */
  public String[] startRTS() throws IOException, InterruptedException {
    JsonArray dataSources = this.dataStore.getDataSources();
    String[] arguments = new String[dataSources.size() + 2];
    arguments[0] = this.dataStore.getHubAddress();
    arguments[1] = this.dataStore.getInstanceName();
    for (int i = 0; i < dataSources.size(); i++) {
      arguments[i + 2] = dataSources.get(i).getAsJsonObject().get("name").getAsString();
    }
    Process p;
    p = this.rt.exec("rts", arguments);
    rtsProcessList.add(p);
    String[] ret = new String[arguments.length + 1];
    ret[0] = "rts";
    for (int i = 0; i < arguments.length; i++) {
      ret[i + 1] = arguments[i];
    }
    return ret;
  }

  /**
   * Starts HFTA processes
   * @throws IOException
   * @throws InterruptedException
   */
  public String[][] startHFTA() throws IOException, InterruptedException {
    int hftaCount = this.dataStore.getHFTACount();
    String[] arguments = {this.dataStore.getHubAddress(), this.dataStore.getInstanceName()};
    for (int i = 0; i < hftaCount; i++) {
      Process p = this.rt.exec("hfta_" + i, arguments);
      this.hftaProcessList.add(p);
    }
    for (int i = 0; i < hftaCount; i++) {
      this.hftaProcessList.get(i).waitFor();
    }
    String[][] ret = new String[hftaCount][arguments.length + 1];
    for (int j = 0; j < hftaCount; j++) {
      ret[j][0] = "hfta_" + j;
      for (int i = 0; i < arguments.length; i++) {
        ret[j][i + 1] = arguments[i];
      }
    }
    return ret;
  }

  /**
   * Starts GSEXIT processes
   * @throws IOException
   * @throws InterruptedException
   */
  public String[][] startGSEXIT() throws IOException, InterruptedException {
    JsonArray dataSinks = this.dataStore.getDataSinks();
    String[] arguments = new String[4];
    arguments[0] = this.dataStore.getHubAddress();
    arguments[1] = this.dataStore.getInstanceName();
    for (int i = 0; i < dataSinks.size(); i++) {
      arguments[2] = dataSinks.get(i).getAsJsonObject().get("fta_name").getAsString();
      arguments[3] = dataSinks.get(i).getAsJsonObject().get("name").getAsString();
      Process p = this.rt.exec("GSEXIT", arguments);
      this.gsExitProcessList.add(p);
    }
    for (int i = 0; i < dataSinks.size(); i++) {
      this.gsExitProcessList.get(i).waitFor();
    }
    String[][] ret = new String[dataSinks.size()][arguments.length + 1];
    for (int j = 0; j < dataSinks.size(); j++) {
      ret[j][0] = "GSEXIT";
      ret[j][1] = this.dataStore.getHubAddress();
      ret[j][2] = this.dataStore.getInstanceName();
      ret[j][3] = dataSinks.get(j).getAsJsonObject().get("fta_name").getAsString();
      ret[j][4] = dataSinks.get(j).getAsJsonObject().get("name").getAsString();
    }
    return ret;
  }

  public String startProcess(String command, String[] arguments) throws IOException, InterruptedException {
    Process p = this.rt.exec(command, arguments);
    return IOUtils.toString(p.getInputStream(), "UTF-8");
  }
}
