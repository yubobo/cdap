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

import com.continuuity.http.AbstractHttpHandler;
import com.continuuity.http.HttpResponder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.Charsets;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


/**
 * HubHttpHandler
 */
@Path("/v1")
public class HubHttpHandler extends AbstractHttpHandler {
  private HubDataStore hubDataStore;

  public HubHttpHandler(HubDataStore ds) {
    hubDataStore = ds;
  }

  private String getStringContent(HttpRequest request) {
    return request.getContent().toString(Charsets.UTF_8);
  }

  public void updateHubDataStore(HubDataStore hubDataStore) {
    this.hubDataStore = hubDataStore;
  }

  @Path("/AnnounceInstance")
  @POST
  public void announceInstance(HttpRequest request, HttpResponder responder) {
    String req;
    JsonObject requestData;
    try {
      req = getStringContent(request);
    } catch (Exception e) {
      throw new RuntimeException("Cannot read HTTP request");
    }
    try {
      JsonParser jsonParser = new JsonParser();
      JsonReader jsonReader = new JsonReader(new StringReader(req));
      jsonReader.setLenient(true);
      requestData = (JsonObject) jsonParser.parse(jsonReader);
    } catch (Exception e) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    updateHubDataStore(new HubDataStore.Builder().copy(hubDataStore)
                         .setInstanceName(requestData.get("name").getAsString()).build());
    updateHubDataStore(new HubDataStore.Builder().copy(hubDataStore)
                         .setClearingHouseAddress(new InetSocketAddress(requestData.get("ip").getAsString(),
                                                                        requestData.get("port").getAsInt())).build());
    responder.sendStatus(HttpResponseStatus.OK);
  }

  @Path("/DiscoverInstance/{instance}")
  @GET
  public void discoverInstance(HttpRequest request, HttpResponder responder,
                               @PathParam("instance") String instance) {
    if (!instance.equals(hubDataStore.getInstanceName())) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    InetSocketAddress address = hubDataStore.getClearingHouseAddress();
    if (address == null) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    JsonObject res = new JsonObject();
    res.addProperty("ip", address.getAddress().getHostAddress());
    res.addProperty("port", address.getPort());
    responder.sendJson(HttpResponseStatus.OK, res);
  }

  @Path("/AnnounceInitializedInstance")
  @POST
  public void announceInitializedInstance(HttpRequest request, HttpResponder responder) {
    String req;
    JsonObject requestData;
    try {
      req = getStringContent(request);
    } catch (Exception e) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      throw new RuntimeException("Cannot read HTTP request");
    }
    try {
      JsonParser jsonParser = new JsonParser();
      JsonReader jsonReader = new JsonReader(new StringReader(req));
      jsonReader.setLenient(true);
      requestData = (JsonObject) jsonParser.parse(jsonReader);
    } catch (Exception e) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      throw new RuntimeException("Cannot parse JSON");
    }
    if (hubDataStore.getInstanceName().equals(requestData.get("name").getAsString())) {
      updateHubDataStore(new HubDataStore.Builder().copy(hubDataStore).initialize().build());
      responder.sendStatus(HttpResponseStatus.OK);
      return;
    }
    responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
  }

  @Path("/DiscoverInitializedInstance/{instance}")
  @GET
  public void discoverInitializedInstance(HttpRequest request, HttpResponder responder,
                                          @PathParam("instance") String instance) {
    if ((!hubDataStore.isInitialized()) || (!instance.equals(hubDataStore.getInstanceName()))) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    InetSocketAddress address = this.hubDataStore.getClearingHouseAddress();
    if (address == null) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    JsonObject res = new JsonObject();
    res.addProperty("ip", address.getAddress().getHostAddress());
    res.addProperty("port", address.getPort());
    responder.sendJson(HttpResponseStatus.OK, res);
  }

  @Path("/DiscoverSource/{dataSourceName}")
  @GET
  public void discoverSource(HttpRequest request, HttpResponder responder,
                             @PathParam("dataSourceName") String dataSourceName) {
    List<HubDataSource> dataSources = hubDataStore.getHubDataSources();
    HubDataSource ds = null;
    for (HubDataSource hds : dataSources) {
      if (dataSourceName.equals(hds.getName())) {
        ds = hds;
        break;
      }
    }
    if (ds == null) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    JsonObject res = new JsonObject();
    res.addProperty("ip", ds.getAddress().getAddress().getHostAddress());
    res.addProperty("port", ds.getAddress().getPort());
    responder.sendJson(HttpResponseStatus.OK, res);
  }

  @Path("/DiscoverSink/{dataSinkName}")
  @GET
  public void discoverSink(HttpRequest request, HttpResponder responder,
                           @PathParam("dataSinkName") String dataSinkName) {
    List<HubDataSink> dataSink = hubDataStore.getHubDataSinks();
    HubDataSink ds = null;
    for (HubDataSink hds : dataSink) {
      if (dataSinkName.equals(hds.getName())) {
        ds = hds;
        break;
      }
    }
    if (ds == null) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    JsonObject res = new JsonObject();
    res.addProperty("ip", ds.getAddress().getAddress().getHostAddress());
    res.addProperty("port", ds.getAddress().getPort());
    responder.sendJson(HttpResponseStatus.OK, res);
  }
}
