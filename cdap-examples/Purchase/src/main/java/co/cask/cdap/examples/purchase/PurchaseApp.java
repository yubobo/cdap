/*
 * Copyright © 2014-2015 Cask Data, Inc.
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

package co.cask.cdap.examples.purchase;

import co.cask.cdap.api.annotation.UseDataSet;
import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.dataset.lib.KeyValueTable;
import co.cask.cdap.api.service.http.AbstractHttpServiceHandler;
import co.cask.cdap.api.service.http.HttpServiceRequest;
import co.cask.cdap.api.service.http.HttpServiceResponder;
import com.google.common.collect.ImmutableMap;

import java.util.Random;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * This implements a simple purchase history application via a scheduled MapReduce Workflow --
 * see package-info for more details.
 */
public class PurchaseApp extends AbstractApplication {

  public static final String APP_NAME = "PurchaseHistory";

  @Override
  public void configure() {
    setName(APP_NAME);
    setDescription("Purchase history application.");

    createDataset("KeyValueStore", KeyValueTable.class);
    addService("KVService", new KVHandler());
  }

  /**
   * 
   */
  public static final class KVHandler extends AbstractHttpServiceHandler {
    @UseDataSet("KeyValueStore")
    private KeyValueTable store;

    @Path("read/{id}")
    @GET
    public void read(HttpServiceRequest request, HttpServiceResponder responder, @PathParam("id") String id) {
      byte[] value = store.read(id);
      if (value != null) {
        responder.sendJson(ImmutableMap.of("key", id, "value", Bytes.toString(value)));
      } else {
        responder.sendStatus(204);
      }
    }

    @Path("write/{key}/{value}")
    @GET
    public void write(HttpServiceRequest request, HttpServiceResponder responder,
                      @PathParam("key") String key, @PathParam("value") String value) {
      store.write(key, value);
      responder.sendStatus(200);
    }

    @Path("write/{key}")
    @GET
    public void write(HttpServiceRequest request, HttpServiceResponder responder, @PathParam("key") String key) {
      store.write(key, String.valueOf(new Random(System.currentTimeMillis()).nextInt()));
      responder.sendStatus(200);
    }
  }
}
