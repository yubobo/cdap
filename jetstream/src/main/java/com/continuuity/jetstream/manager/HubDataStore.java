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

import java.net.InetSocketAddress;
import java.util.List;

/**
 * dataStore holds all information associated with a single GSFlowlet
 */

public class HubDataStore {


  private final String instanceName;
  private final boolean isInitialized;
  private final List<HubDataSource> hubDataSources;
  private final List<HubDataSink> hubDataSinks;
  private final int hftaCount;
  private final InetSocketAddress hubAddress;
  private final InetSocketAddress clearingHouseAddress;

  public HubDataStore(String name, boolean initialized, List<HubDataSource> sourceList, List<HubDataSink> sinkList,
                      int count, InetSocketAddress hubAddress, InetSocketAddress clearingHouseAddress) {
    this.instanceName = name;
    this.isInitialized = initialized;
    this.hubDataSources = sourceList;
    this.hubDataSinks = sinkList;
    this.hftaCount = count;
    this.hubAddress = hubAddress;
    this.clearingHouseAddress = clearingHouseAddress;
  }

  /**
   * Returns initialization state for this instance
   */
  public boolean isInitialized() {
    return isInitialized;
  }

  /**
   * @return list of data source
   */
  public List<HubDataSource> getHubDataSources() {
    return hubDataSources;
  }

  /**
   * @return list of data sinks
   */
  public List<HubDataSink> getHubDataSinks() {
    return hubDataSinks;
  }

  /**
   * @return the gsInstance name as a String
   */
  public String getInstanceName() {
    return instanceName;
  }

  /**
   * @return the hub address
   */
  public InetSocketAddress getHubAddress() {
    return hubAddress;
  }

  /**
   * @return the number of HFTA
   */
  public int getHFTACount() {
    return hftaCount;
  }

  public InetSocketAddress getClearingHouseAddress() {
    return clearingHouseAddress;
  }
}

