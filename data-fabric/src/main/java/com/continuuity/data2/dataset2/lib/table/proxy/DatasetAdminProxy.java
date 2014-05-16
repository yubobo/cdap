package com.continuuity.data2.dataset2.lib.table.proxy;

import com.continuuity.common.conf.Constants;
import com.continuuity.internal.data.dataset.DatasetAdmin;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Forwards {@link DatasetAdmin} operations to a {@link DatasetUserService} via REST APIs.
 */
public class DatasetAdminProxy implements DatasetAdmin {

  private static final Gson GSON = new Gson();

  private final InetSocketAddress address;
  private final String instanceName;

  public DatasetAdminProxy(InetSocketAddress address, String instanceName) {
    this.address = address;
    this.instanceName = instanceName;
  }

  @Override
  public boolean exists() throws IOException {
    HttpResponse response = doGet(String.format("/%s/datasets/user/admin/%s/exists",
                                                Constants.Dataset.User.VERSION, instanceName));
    return GSON.fromJson(new InputStreamReader(response.getEntity().getContent()), Boolean.class);
  }

  @Override
  public void create() throws IOException {
    // TODO
  }

  @Override
  public void drop() throws IOException {

  }

  @Override
  public void truncate() throws IOException {

  }

  @Override
  public void upgrade() throws IOException {

  }

  @Override
  public void close() throws IOException {

  }

  private HttpResponse doGet(String path) throws IOException {
    URI uri = null;

    try {
      uri = new URL("http", address.getHostName(), address.getPort(), path).toURI();
    } catch (URISyntaxException e) {
      Throwables.propagate(e);
    }

    HttpClient httpClient = new DefaultHttpClient();
    HttpGet getMethod = new HttpGet(uri);
    return httpClient.execute(getMethod);
  }
}
