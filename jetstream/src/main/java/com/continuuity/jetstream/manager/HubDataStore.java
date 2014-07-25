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

  private HubDataStore(Builder builder) {
    this.instanceName = builder.instanceName;
    this.isInitialized = builder.isInitialized;
    this.hftaCount = builder.hftaCount;
    this.hubAddress = builder.hubAddress;
    this.clearingHouseAddress = builder.clearingHouseAddress;
    if (builder.hubDataSources != null) {
      this.hubDataSources = Lists.newArrayList(builder.hubDataSources);
    } else {
      this.hubDataSources = null;
    }
    if (builder.hubDataSinks != null) {
      this.hubDataSinks = Lists.newArrayList(builder.hubDataSinks);
    } else {
      this.hubDataSinks = null;
    }
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

  /**
   * Builder to build immutable HubDataStoreObjects
   */
  public static class Builder {
    private String instanceName;
    private boolean isInitialized;
    private List<HubDataSource> hubDataSources;
    private List<HubDataSink> hubDataSinks;
    private int hftaCount;
    private InetSocketAddress hubAddress;
    private InetSocketAddress clearingHouseAddress;

    public Builder() {
      this.isInitialized = false;
    }

    public Builder setInstanceName(String name) {
      this.instanceName = name;
      return this;
    }

    public Builder initialize() {
      this.isInitialized = true;
      return this;
    }

    public Builder setHFTACount(int n) {
      this.hftaCount = n;
      return this;
    }

    public Builder setHubAddress(InetSocketAddress address) {
      this.hubAddress = address;
      return this;
    }

    public Builder setClearingHouseAddress(InetSocketAddress address) {
      this.clearingHouseAddress = address;
      return this;
    }

    public Builder addDataSource(List<HubDataSource> sourceList) {
      this.hubDataSources = sourceList;
      return this;
    }

    public Builder addDataSink(List<HubDataSink> sinkList) {
      this.hubDataSinks = sinkList;
      return this;
    }

    public HubDataStore build() {
      return new HubDataStore(this);
    }

    public Builder copy(HubDataStore hubDataStore) {
      this.instanceName = hubDataStore.instanceName;
      this.isInitialized = hubDataStore.isInitialized;
      this.hubDataSources = hubDataStore.hubDataSources;
      this.hubDataSinks = hubDataStore.hubDataSinks;
      this.hftaCount = hubDataStore.hftaCount;
      this.hubAddress = hubDataStore.hubAddress;
      this.clearingHouseAddress = hubDataStore.clearingHouseAddress;
      return this;
    }

  }
}

