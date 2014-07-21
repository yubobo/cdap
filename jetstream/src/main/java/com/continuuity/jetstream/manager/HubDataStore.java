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
import java.util.ArrayList;
import java.util.List;

/**
 * dataStore holds all information associated with a single GSFlowlet
 */

public class HubDataStore {

  private final List<HubDataSource> hubDataSources;
  private final List<HubDataSink> hubDataSinks;
  private String instanceName;
  private int hftaCount;
  private InetSocketAddress hubAddress;
  private InetSocketAddress clearingHouseAddress;
  private boolean isInitialized;

  public HubDataStore() {
    hubDataSources = new ArrayList<HubDataSource>();
    hubDataSinks = new ArrayList<HubDataSink>();
    isInitialized = false;
  }


  /**
   * Returns initialization state for this instance
   */
  public boolean isInitialized() {
    return isInitialized;
  }

  /**
   * Set flag as true when the "AnnounceInitializedInstance" is called
   */
  public void initialize() {
    isInitialized = true;
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

  /**
   * Set hub address
   * @param address Hub address
   */
  public void setHubAddress(InetSocketAddress address) {
    this.hubAddress = address;
  }

  public void setClearingHouseAddress(InetSocketAddress address) {
    clearingHouseAddress = address;
  }

  /**
   * Add Data Source
   * @param dsName data sink name
   * @param address IP:port of the data store
   */
  public void addDataSource(String dsName, InetSocketAddress address) {
    hubDataSources.add(new HubDataSource(dsName, address));
  }

  /**
   * Add Data Sink
   * @param dsName data sink name
   * @param ftaName FTA Name
   * @param address IP:port of the data store
   */
  public void addDataSink(String dsName, String ftaName, InetSocketAddress address) {
    hubDataSinks.add(new HubDataSink(dsName, ftaName, address));
  }

  /**
   * Set Instance name
   * @param instanceName Instance Name
   */
  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  /**
   * Set HFTA Count
   * @param count number of HFTA
   */
  public void setHFTACount(int count) {
   this.hftaCount = count;
  }
}

