package com.continuuity.data2.dataset2.lib.table.proxy;

import com.continuuity.common.conf.Constants;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public abstract class DatasetAdminProxy implements DatasetAdmin {

  private static final Gson GSON = new Gson();

  public static String getDiscoverableName(String user) {
    return Constants.Service.DATASET_USER_ADMIN + "." + user;
  }

  public abstract String getHostAndPort();
  public abstract void cleanup();

  @Override
  public boolean exists() throws IOException {
    String name = "test123";
    HttpClient httpClient = new DefaultHttpClient();
    HttpResponse httpResponse = httpClient.execute(
      new HttpGet(getHostAndPort() + "/v1/datasets/user/" + name + "/exists"));
    Preconditions.checkState(httpResponse.getStatusLine().getStatusCode() == 200);
    return GSON.fromJson(new InputStreamReader(httpResponse.getEntity().getContent()), Boolean.class);
  }

  @Override
  public void create() throws IOException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void drop() throws IOException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void truncate() throws IOException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void upgrade() throws IOException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void close() throws IOException {
    throw new UnsupportedOperationException("TODO");
  }
}
