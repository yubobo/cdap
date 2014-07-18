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
import com.google.gson.JsonObject;

/**
 * dataStore holds all information associated with a single GSFlowlet
 */

public class DataStore {
  /**
   * JSON Object that holds all the data
   */
  private JsonObject data;

  public DataStore() {
    data = new JsonObject();
  }

  /**
   * @return list of data source as an array of JSONObjects of the format {'name':<dataSourceName>, 'address':<IP:port>}
   */
  public JsonArray getDataSources() {
    return data.getAsJsonArray("dataSource");
  }

  /**
   * @return list of data sinks as an array of JSONObjects of the format {'name':<dataSourceName>, 'address':<IP:port>}
   */
  public JsonArray getDataSinks() {
    return data.getAsJsonArray("dataSink");
  }

  /**
   * @return the gsInstance name as a String
   */
  public String getInstanceName() {
    return data.get("instanceName").getAsString();
  }

  /**
   * @return the hub IP and port address as a string in the format <IP:Port>
   */
  public String getHubAddress() {
    return data.get("hubAddres").getAsString();
  }

  /**
   * @return the number of HFTA
   */
  public int getHFTACount() {
    return data.get("hfta").getAsInt();
  }

  public String getClearingHouseAddress() {
    return data.get("clearingHouseAddress").getAsString();
  }

  public boolean setClearingHouseAddress(String ip) {
    try {
      data.addProperty("clearingHouseAddress", ip);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Stores data sink in format
   * {'dataSource' : [{'name' : <dsName1>, 'address' : <address1>},
   *                  {'name' : <dsName2>, 'address' : <address2>},
   *                  {'name' : <dsName3>, 'address' : <address3>},
   *                  ..
   *                  ..
   *                  {'name' : <dsNameN>, 'address' : <addressN>}
   *                  ]}
   *
   * @param dsName data sink name
   * @param address IP:port of the data store
   * @return true if success false otherwise
   */
  public boolean addDataSource(String dsName, String address) {
    try {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("name", dsName);
      jsonObject.addProperty("address", address);
      JsonArray dsList = data.getAsJsonArray("dataSource");
      if (dsList == null) {
        dsList = new JsonArray();
      }
      dsList.add(jsonObject);
      data.add("dataSource", dsList);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Stores data sink in format
   * {'dataSource' : [{'name' : <dsName1>, 'fta_name': <ftaName1>, 'address' : <address1>},
   *                  {'name' : <dsName2>, 'fta_name': <ftaName2>, 'address' : <address2>},
   *                  {'name' : <dsName3>, 'fta_name': <ftaName3>, 'address' : <address3>},
   *                  ..
   *                  ..
   *                  {'name' : <dsNameN>, 'fta_name': <ftaNameN>, 'address' : <addressN>}
   *                  ]}
   *
   * @param dsName data sink name
   * @param ftaName FTA Name
   * @param address IP:port of the data store
   * @return true if success false otherwise
   */
  public boolean addDataSink(String dsName, String ftaName, String address) {
    try {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("name", dsName);
      jsonObject.addProperty("fta_name", ftaName);
      jsonObject.addProperty("address", address);
      JsonArray dsList = data.getAsJsonArray("dataSink");
      if (dsList == null) {
        dsList = new JsonArray();
      }
      dsList.add(jsonObject);
      data.add("dataSink", dsList);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Set hub address
   * @param hubAddress Hub address in the format <IP:Port>
   * @return true if success false otherwise
   */
  public boolean setHubAddress(String hubAddress) {
    try {
      data.addProperty("hubAddres", hubAddress);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Set Instance name
   * @param instanceName Instance Name
   * @return true if success false otherwise
   */
  public boolean setInstanceName(String instanceName) {
    try {
      data.addProperty("instanceName", instanceName);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Set HFTA Count
   * @param count number of HFTA
   * @return true if success false otherwise
   */
  public boolean setHFTACount(int count) {
    try {
      data.addProperty("hfta", count);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
