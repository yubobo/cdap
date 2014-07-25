/*
 * Copyright 2014 Continuuity, Inc.
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

package com.continuuity.shell;

import com.continuuity.client.config.ReactorClientConfig;
import com.continuuity.shell.command.VersionCommand;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Configuration for Reactor shell.
 */
public class ReactorShellConfig {

  public static final int PORT = 10000;

  private final ReactorClientConfig reactorConfig;
  private final String version;

  private String reactorHost;
  private List<ReactorHostChangeListener> reactorHostChangeListeners;

  /**
   * @param reactorHost Hostname of the Reactor instance to interact with (e.g. "example.com")
   * @throws java.net.URISyntaxException
   */
  public ReactorShellConfig(String reactorHost) throws URISyntaxException {
    this.reactorHost = Objects.firstNonNull(reactorHost, "localhost");
    this.reactorConfig = new ReactorClientConfig(reactorHost, PORT);
    this.version = tryGetVersion();
    this.reactorHostChangeListeners = Lists.newArrayList();
  }

  private static String tryGetVersion() {
    try {
      InputSupplier<? extends InputStream> versionFileSupplier = new InputSupplier<InputStream>() {
        @Override
        public InputStream getInput() throws IOException {
          return VersionCommand.class.getClassLoader().getResourceAsStream("VERSION");
        }
      };
      return CharStreams.toString(CharStreams.newReaderSupplier(versionFileSupplier, Charsets.UTF_8));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public String getReactorHost() {
    return reactorHost;
  }

  public ReactorClientConfig getReactorConfig() {
    return reactorConfig;
  }

  public String getVersion() {
    return version;
  }

  public void setReactorHost(String reactorHost) throws URISyntaxException {
    this.reactorHost = reactorHost;
    this.reactorConfig.setReactorHost(reactorHost, PORT);
    for (ReactorHostChangeListener listener : reactorHostChangeListeners) {
      listener.onReactorHostChanged(reactorHost);
    }
  }

  public void addReactorHostChangeListener(ReactorHostChangeListener listener) {
    this.reactorHostChangeListeners.add(listener);
  }

  /**
   * Listener for reactorHost changes.
   */
  public interface ReactorHostChangeListener {
    void onReactorHostChanged(String newReactorHost);
  }
}
