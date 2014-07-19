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

/**
 * Class to store HubDataSink Information
 */
class HubDataSink extends HubDataSource {
  private String ftaName;

  public HubDataSink(String name, String ftaName, InetSocketAddress address) {
    super(name, address);
    this.ftaName = ftaName;
  }

  public void setFtaName(String name) {
    this.ftaName = name;
  }

  public String getFtaName() {
    return ftaName;
  }
}
