package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.Constants;
import com.continuuity.internal.data.dataset.DatasetInstanceProperties;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 *
 */
public class DatasetAdminHandlerTest extends DatasetInstanceHandlerTest {

  private static final Gson GSON = new Gson();

  @Test
  public void testExists() throws Exception {
    String datasetName = "dataset1";
    String datasetType = "orderedTable";

    // verify instance does not exist
    Response<Boolean> existsResponse = getExists(datasetName);
    Assert.assertEquals(HttpStatus.SC_OK, existsResponse.status);
    Assert.assertEquals(false, existsResponse.value);

    // create dataset instance
    DatasetInstanceProperties props = new DatasetInstanceProperties.Builder().property("prop1", "val1").build();
    Assert.assertEquals(HttpStatus.SC_OK, createInstance("continuuity.user." + datasetName, datasetType, props));

    // verify instance still does not exist (since not created physically)
    existsResponse = getExists(datasetName);
    Assert.assertEquals(HttpStatus.SC_OK, existsResponse.status);
    Assert.assertEquals(false, existsResponse.value);
  }

  protected static String getUrl(String resource) {
    return "http://" + "localhost" + ":" + Constants.Dataset.UserService.DEFAULT_PORT +
      "/" + Constants.Dataset.UserService.VERSION + resource;
  }

  protected Response<Boolean> getExists(String instanceName) throws IOException {
    HttpGet post = new HttpGet(getUrl("/datasets/admin/" + instanceName + "/exists"));

    DefaultHttpClient client = new DefaultHttpClient();
    HttpResponse response = client.execute(post);
    return parseResponse(response, Boolean.class);
  }
}