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
import com.google.common.io.Closeables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ProcessInitiator is responsible for initiating all processes required by Streaming Engine
 */

public class ProcessInitiator {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessInitiator.class);
  private final HubDataStore hubDataStore;
  private final List<Process> rtsProcessList;
  private final List<Process> hftaProcessList;
  private final List<Process> gsExitProcessList;
  private final List<ExecutorService> rtsExecutor;
  private final List<ExecutorService> hftaExecutor;
  private final List<ExecutorService> gsExitExecutor;


  /**
   * Constructs ProcessInitiator object for the specified HubDataStore
   * @param ds associated HubDataStore
   */
  public ProcessInitiator(HubDataStore ds) {
    hubDataStore = ds;
    rtsProcessList = Lists.newArrayList();
    hftaProcessList = Lists.newArrayList();
    gsExitProcessList = Lists.newArrayList();
    rtsExecutor = Lists.newArrayList();
    hftaExecutor = Lists.newArrayList();
    gsExitExecutor = Lists.newArrayList();
  }

  /**
   * Sequentially invokes the required Streaming Engine processes
   */
  public void init() {
    try {
      startRTS();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot initiate RTS. Missing or bad arguments");
    }
    try {
      startHFTA();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot initiate HFTA processes. Missing or bad arguments");
    }
    try {
      startGSEXIT();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot initiate GSEXIT processes. Missing or bad arguments");
    }
  }

  /**
   * Starts RTS process
   * @throws IOException
   */
  public void startRTS() throws IOException {
    List<HubDataSource> dataSources = hubDataStore.getHubDataSources();
    List<String> com = Lists.newArrayList();
    com.add("rts");
    com.add(hubDataStore.getHubAddress().toString());
    com.add(hubDataStore.getInstanceName());
    for (HubDataSource source : dataSources) {
      com.add(source.getName());
    }
    ProcessBuilder builder = new ProcessBuilder(com);
    builder.redirectErrorStream(true);
    Process p = builder.start();
    rtsProcessList.add(p);
    ExecutorService executorService = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
      .setDaemon(true).setNameFormat("process-rts-%d").build());
    rtsExecutor.add(executorService);
    executorService.execute(createLogRunnable(p, "rts"));
  }

  /**
   * Starts HFTA processes
   * @throws IOException
   */
  public void startHFTA() throws IOException {
    int hftaCount = hubDataStore.getHFTACount();
    for (int i = 0; i < hftaCount; i++) {
      List<String> com = Lists.newArrayList();
      com.add("hfta_" + i);
      com.add(hubDataStore.getHubAddress().toString());
      com.add(hubDataStore.getInstanceName());
      ProcessBuilder builder = new ProcessBuilder(com);
      builder.redirectErrorStream(true);
      Process p = builder.start();
      hftaProcessList.add(p);
      ExecutorService executorService = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
        .setDaemon(true).setNameFormat("process-rts-%d").build());
      hftaExecutor.add(executorService);
      executorService.execute(createLogRunnable(p, "hfta_" + i));
    }
  }

  /**
   * Starts GSEXIT processes
   * @throws IOException
   */
  public void startGSEXIT() throws IOException {
    List<HubDataSink> dataSinks = hubDataStore.getHubDataSinks();
    for (int i = 0; i < dataSinks.size(); i++) {
      List<String> com = Lists.newArrayList();
      com.add("GSEXIT");
      com.add(hubDataStore.getHubAddress().toString());
      com.add(hubDataStore.getInstanceName());
      com.add(dataSinks.get(i).getFTAName());
      com.add(dataSinks.get(i).getName());
      ProcessBuilder builder = new ProcessBuilder(com);
      builder.redirectErrorStream(true);
      Process p = builder.start();
      gsExitProcessList.add(p);
      ExecutorService executorService = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder()
        .setDaemon(true).setNameFormat("process-rts-%d").build());
      gsExitExecutor.add(executorService);
      executorService.execute(createLogRunnable(p, "GSEXIT(" + i + ")"));
    }
  }

  private Runnable createLogRunnable(final Process process, final String processName) {
    return new Runnable() {
      @Override
      public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charsets.UTF_8));
        try {
          String line = reader.readLine();
          while (!Thread.currentThread().isInterrupted() && line != null) {
            LOG.info("[" + processName + "]" + line);
            line = reader.readLine();
          }
        } catch (IOException e) {
          LOG.error("[" + processName + "]" + "Exception when reading stream output and error log for {}.", ProcessInitiator.this);
        } finally {
          Closeables.closeQuietly(reader);
        }
      }
    };
  }

  public void waitForRTS() throws Exception {
    for (Process p : rtsProcessList) {
      p.waitFor();
    }
  }

  public void waitForHFTA() throws Exception {
    for (Process p : hftaProcessList) {
      p.waitFor();
    }
  }

  public void waitForGSEXIT() throws Exception {
    for (Process p : gsExitProcessList) {
      p.waitFor();
    }
  }
}
