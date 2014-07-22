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

import com.continuuity.http.HttpHandler;
import com.continuuity.http.NettyHttpService;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractIdleService;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * DiscoveryServer
 */

class DiscoveryServer extends AbstractIdleService {
  private NettyHttpService service;
  private HubDataStore hubDataStore;
  private InetSocketAddress serviceAddress;

  @Override
  public void startUp() {
    List<HttpHandler> handlers = Lists.newArrayList();
    HubHttpHandler handler = new HubHttpHandler(this.hubDataStore);
    handlers.add(handler);
    NettyHttpService.Builder builder = NettyHttpService.builder();
    builder.addHttpHandlers(handlers);
    builder.setHttpChunkLimit(75 * 1024);
    service = builder.build();
    service.startAndWait();
    serviceAddress = new InetSocketAddress(service.getBindAddress().getAddress().getHostAddress(), service.getBindAddress().getPort());
    handler.updateHubDataStore(HubDataStoreFactory.setHubAddress(hubDataStore, this.serviceAddress));
  }

  @Override
  public void shutDown() {
    service.stopAndWait();
  }

  public DiscoveryServer(HubDataStore ds) {
    this.hubDataStore = ds;
  }

  protected void finalize() throws Throwable {
    super.finalize();
    service.stopAndWait();
  }

  public InetSocketAddress getHubAddress() {
    return this.serviceAddress;
  }
}
