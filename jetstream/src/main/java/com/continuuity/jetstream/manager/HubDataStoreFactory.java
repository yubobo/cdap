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
 * HubDataStoreFactory
 */

public class HubDataStoreFactory {

  public static HubDataStore setInstanceName(HubDataStore hubDataStore, String name) {
    return new HubDataStore(name, hubDataStore.isInitialized(), hubDataStore.getHubDataSources(),
                                    hubDataStore.getHubDataSinks(), hubDataStore.getHFTACount(),
                                    hubDataStore.getHubAddress(), hubDataStore.getClearingHouseAddress());
  }

  public static HubDataStore setHubAddress(HubDataStore hubDataStore, InetSocketAddress hubAddress) {
    return new HubDataStore(hubDataStore.getInstanceName(), hubDataStore.isInitialized(),
                            hubDataStore.getHubDataSources(), hubDataStore.getHubDataSinks(),
                            hubDataStore.getHFTACount(), hubAddress, hubDataStore.getClearingHouseAddress());
  }

  public static HubDataStore setClearingHouseAddress(HubDataStore hubDataStore, InetSocketAddress address) {
    return new HubDataStore(hubDataStore.getInstanceName(), hubDataStore.isInitialized(),
                                    hubDataStore.getHubDataSources(), hubDataStore.getHubDataSinks(),
                                    hubDataStore.getHFTACount(), hubDataStore.getHubAddress(), address);
  }

  public static HubDataStore initialize(HubDataStore hubDataStore) {
    return new HubDataStore(hubDataStore.getInstanceName(), true, hubDataStore.getHubDataSources(),
                                    hubDataStore.getHubDataSinks(), hubDataStore.getHFTACount(),
                                    hubDataStore.getHubAddress(), hubDataStore.getClearingHouseAddress());
  }

  public static HubDataStore setHFTACount(HubDataStore hubDataStore, int count) {
    return new HubDataStore(hubDataStore.getInstanceName(), hubDataStore.isInitialized(),
                            hubDataStore.getHubDataSources(), hubDataStore.getHubDataSinks(), count,
                            hubDataStore.getHubAddress(), hubDataStore.getClearingHouseAddress());
  }

  public static HubDataStore addDataSource(HubDataStore hubDataStore, List<HubDataSource> sourceList) {
    List<HubDataSource> tmpList = hubDataStore.getHubDataSources();
    if (tmpList == null) {
      tmpList = Lists.newArrayList();
    }
    for (HubDataSource source : sourceList) {
      tmpList.add(source);
    }
    return new HubDataStore(hubDataStore.getInstanceName(), hubDataStore.isInitialized(), tmpList,
                            hubDataStore.getHubDataSinks(), hubDataStore.getHFTACount(), hubDataStore.getHubAddress(),
                            hubDataStore.getClearingHouseAddress());
  }

  public static HubDataStore addDataSink(HubDataStore hubDataStore, List<HubDataSink> sinkList) {
    List<HubDataSink> tmpList = hubDataStore.getHubDataSinks();
    if (tmpList == null) {
      tmpList = Lists.newArrayList();
    }
    for (HubDataSink sink : sinkList) {
      tmpList.add(sink);
    }
    return new HubDataStore(hubDataStore.getInstanceName(), hubDataStore.isInitialized(),
                            hubDataStore.getHubDataSources(), tmpList, hubDataStore.getHFTACount(),
                            hubDataStore.getHubAddress(), hubDataStore.getClearingHouseAddress());
  }
}
