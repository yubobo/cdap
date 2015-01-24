/*
 * Copyright © 2015 Cask Data, Inc.
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
import co.cask.cdap.api.service.AbstractService;
import co.cask.cdap.api.service.http.AbstractHttpServiceHandler;
import co.cask.cdap.api.service.http.HttpServiceRequest;
import co.cask.cdap.api.service.http.HttpServiceResponder;
import com.google.gson.Gson;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *
 */
public class RecordService extends AbstractService {

  @Override
  protected void configure() {
    setName("RecordService");
    addHandler(new RecordHandler());
  }

  /**
   *
   */
  public static final class RecordHandler extends AbstractHttpServiceHandler {
    private static final Gson GSON = new Gson();

    @UseDataSet("recordDataset")
    private RecordDataset recordDataset;

    @Path("read/{key}")
    @GET
    public void read(HttpServiceRequest request, HttpServiceResponder responder, @PathParam("key") String key) {
      responder.sendJson(GSON.toJson(recordDataset.getRecord(key)));
    }

    @Path("write/{key}")
    @POST
    public void write(HttpServiceRequest request, HttpServiceResponder responder, @PathParam("key") String key) {
      recordDataset.writeRecord(key, new Record("id", "firstName", "lastName"));
      responder.sendStatus(200);
    }
  }
}
