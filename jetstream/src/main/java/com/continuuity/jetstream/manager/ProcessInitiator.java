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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProcessInitiator is responsible for initiating all processes required by GigaScope
 */

public class ProcessInitiator {
  private final HubDataStore hubDataStore;
  private final Runtime rt;
  private final List<Process> rtsProcessList;
  private final List<Process> hftaProcessList;
  private final List<Process> gsExitProcessList;

  /**
   * Constructs ProcessInitiator object for the specified HubDataStore
   * @param ds associated HubDataStore
   */
  public ProcessInitiator(HubDataStore ds) {
    this.hubDataStore = ds;
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
   */
  public void startRTS() throws IOException {
    List<HubDataSource> dataSources = this.hubDataStore.getHubDataSources();
    String[] arguments = new String[dataSources.size() + 2];
    arguments[0] = this.hubDataStore.getHubAddress().toString();
    arguments[1] = this.hubDataStore.getInstanceName();
    for (int i = 0; i < dataSources.size(); i++) {
      arguments[i + 2] = dataSources.get(i).getName();
    }
    Process p;
    p = this.rt.exec("rts", arguments);
    rtsProcessList.add(p);
  }

  /**
   * Starts HFTA processes
   * @throws IOException
   */
  public void startHFTA() throws IOException {
    int hftaCount = this.hubDataStore.getHFTACount();
    String[] arguments = {this.hubDataStore.getHubAddress().toString(), this.hubDataStore.getInstanceName()};
    for (int i = 0; i < hftaCount; i++) {
      Process p = this.rt.exec("hfta_" + i, arguments);
      this.hftaProcessList.add(p);
    }
  }

  /**
   * Starts GSEXIT processes
   * @throws IOException
   */
  public void startGSEXIT() throws IOException {
    List<HubDataSink> dataSinks = this.hubDataStore.getHubDataSinks();
    String[] arguments = new String[4];
    arguments[0] = this.hubDataStore.getHubAddress().toString();
    arguments[1] = this.hubDataStore.getInstanceName();
    for (int i = 0; i < dataSinks.size(); i++) {
      arguments[2] = dataSinks.get(i).getFtaName();
      arguments[3] = dataSinks.get(i).getName();
      Process p = this.rt.exec("GSEXIT", arguments);
      this.gsExitProcessList.add(p);
    }
  }

  public String startProcess(String command, String[] arguments) throws IOException, InterruptedException {
    Process p = this.rt.exec(command, arguments);
    return IOUtils.toString(p.getInputStream(), "UTF-8");
  }
}
