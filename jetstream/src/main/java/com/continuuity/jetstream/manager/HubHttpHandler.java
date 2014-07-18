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

import com.continuuity.http.HandlerContext;
import com.continuuity.http.HttpHandler;
import com.continuuity.http.HttpResponder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


/**
 * HubHttpHandler
 */

@Path("/v1")
public class HubHttpHandler implements HttpHandler {
  private DataStore dataStore;

  public HubHttpHandler(DataStore ds) {
    this.dataStore = ds;
  }

  private String getStringContent(HttpRequest request) throws IOException {
    return IOUtils.toString(new ChannelBufferInputStream(request.getContent()));
  }

  @Override
  public void init(HandlerContext context) {}

  @Override
  public void destroy(HandlerContext context) {}


  @Path("/AnnounceInstance")
  @POST
  public void announceInstance(HttpRequest request, HttpResponder responder) {
    String req;
    try {
      req = new String(getStringContent(request));
    } catch (Exception e) {
      throw new RuntimeException("Cannot read HTTP request");
    }
    JsonParser jsonParser = new JsonParser();
    JsonObject requestData;
    try {
      requestData = (JsonObject) jsonParser.parse(req);
    } catch (Exception e) {
      //throw new RuntimeException("Cannot read HTTP request");
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    this.dataStore.setInstanceName(requestData.get("name").getAsString());
    this.dataStore.setClearingHouseAddress(requestData.get("ip").getAsString() + ":" +
                                             requestData.get("port").getAsString());
    responder.sendStatus(HttpResponseStatus.OK);
  }

  @Path("/DiscoverInstance/{instance}")
  @GET
  public void discoverInstance(HttpRequest request, HttpResponder responder, @PathParam("instance") String instance) {
    String address = new String(this.dataStore.getClearingHouseAddress());
    if (address == null) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    String ip = address.split(":")[0];
    String port = address.split(":")[1];
    JsonObject res = new JsonObject();
    res.addProperty("ip", ip);
    res.addProperty("port", port);
    responder.sendJson(HttpResponseStatus.OK, res);
  }

  @Path("/AnnounceInitializedInstance")
  @POST
  public void announceInitializedInstance(HttpRequest request, HttpResponder responder) {
    String req;
    try {
      req = new String(getStringContent(request));
    } catch (Exception e) {
      throw new RuntimeException("Cannot read HTTP request");
    }
    JsonParser jsonParser = new JsonParser();
    JsonObject requestData = (JsonObject) jsonParser.parse(req);
    this.dataStore.setInstanceName(requestData.get("name").getAsString());
    responder.sendStatus(HttpResponseStatus.OK);
  }

  @Path("/DiscoverInitializedInstance/{instance}")
  @GET
  public void discoverInitializedInstance(HttpRequest request, HttpResponder responder,
                                          @PathParam("instance") String instance) {
    String address = new String(this.dataStore.getClearingHouseAddress());
    if (address == null) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    String ip = new String(address.split(":")[0]);
    String port = new String(address.split(":")[1]);
    JsonObject res = new JsonObject();
    res.addProperty("ip", ip);
    res.addProperty("port", port);
    responder.sendJson(HttpResponseStatus.OK, res);
  }

  @Path("/DiscoverSource/{dataSourceName}")
  @GET
  public void discoverSource(HttpRequest request, HttpResponder responder,
                                          @PathParam("dataSourceName") String dataSourceName) {
    JsonArray dataSources = this.dataStore.getDataSources();
    JsonObject ds = null;
    for (int i = 0; i < dataSources.size(); i++) {
      if (dataSourceName.equals(dataSources.get(i).getAsJsonObject().get("name").getAsString())) {
        ds = dataSources.get(i).getAsJsonObject();
        break;
      }
    }
    if (ds == null) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    String address = new String(ds.get("address").getAsString());
    String ip = new String(address.split(":")[0]);
    String port = new String(address.split(":")[1]);
    JsonObject res = new JsonObject();
    res.addProperty("ip", ip);
    res.addProperty("port", port);
    responder.sendJson(HttpResponseStatus.OK, res);
  }

  @Path("/DiscoverSink/{dataSinkName}")
  @GET
  public void discoverSink(HttpRequest request, HttpResponder responder,
                           @PathParam("dataSinkName") String dataSinkName) {
    JsonArray dataSink = this.dataStore.getDataSinks();
    JsonObject ds = null;
    for (int i = 0; i < dataSink.size(); i++) {
      if (dataSinkName.equals(dataSink.get(i).getAsJsonObject().get("name").getAsString())) {
        ds = dataSink.get(i).getAsJsonObject();
        break;
      }
    }
    if (ds == null) {
      responder.sendStatus(HttpResponseStatus.BAD_REQUEST);
      return;
    }
    String address = new String(ds.get("address").getAsString());
    String ip = new String(address.split(":")[0]);
    String port = new String(address.split(":")[1]);
    JsonObject res = new JsonObject();
    res.addProperty("ip", ip);
    res.addProperty("port", port);
    responder.sendJson(HttpResponseStatus.OK, res);
  }
}
