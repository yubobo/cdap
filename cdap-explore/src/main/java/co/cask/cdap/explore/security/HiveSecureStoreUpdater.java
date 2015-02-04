/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.explore.security;

import co.cask.cdap.common.conf.Constants;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.Credentials;
import org.apache.twill.api.RunId;
import org.apache.twill.api.SecureStore;
import org.apache.twill.api.SecureStoreUpdater;
import org.apache.twill.filesystem.LocationFactory;
import org.apache.twill.internal.yarn.YarnUtils;
import org.apache.twill.yarn.YarnSecureStore;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A {@link SecureStoreUpdater} that provides update to Hive secure token.
 */
public class HiveSecureStoreUpdater implements SecureStoreUpdater {

  private final Configuration conf;
  private final LocationFactory locationFactory;
  private long nextUpdateTime = -1;
  private Credentials credentials;

  @Inject
  public HiveSecureStoreUpdater(Configuration conf, LocationFactory locationFactory) {
    this.conf = conf;
    this.locationFactory = locationFactory;
    this.credentials = new Credentials();
  }

  private void refreshCredentials() {
    try {
      HiveTokenUtils.obtainToken(credentials);
      YarnUtils.addDelegationTokens(conf, locationFactory, credentials);
    } catch (IOException ioe) {
      throw Throwables.propagate(ioe);
    }
  }

  /**
   * Returns the update interval for the Hive delegation token.
   * @return The update interval in milliseconds.
   */
  public long getUpdateInterval() {
    // TODO change that using hive settings - this has just been copied from the HBaseSecureStoreUpdater

    // The value contains in hbase-default.xml, so it should always there. If it is really missing, default it to 1 day.
    return conf.getLong(Constants.HBase.AUTH_KEY_UPDATE_INTERVAL, TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
  }

  @Override
  public SecureStore update(String application, RunId runId) {
    long now = System.currentTimeMillis();
    if (now >= nextUpdateTime) {
      nextUpdateTime = now + getUpdateInterval();
      refreshCredentials();
    }
    return YarnSecureStore.create(credentials);
  }
}
