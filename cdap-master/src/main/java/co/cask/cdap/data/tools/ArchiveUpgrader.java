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

package co.cask.cdap.data.tools;

import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.io.Locations;
import co.cask.cdap.logging.LoggingConfiguration;
import com.google.inject.Inject;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Class to Upgrade the archive directories
 */
public class ArchiveUpgrader extends AbstractUpgrader {

  private static final Logger LOG = LoggerFactory.getLogger(ArchiveUpgrader.class);
  private final CConfiguration cConf;

  @Inject
  private ArchiveUpgrader(LocationFactory locationFactory, CConfiguration cConf) {
    super(locationFactory);
    this.cConf = cConf;
  }

  @Override
  public void upgrade() throws Exception {
    upgradeStream();
    upgradeLogs();
  }

  /**
   * Upgrades the streams archive path
   */
  private void upgradeStream() throws IOException {
    LOG.info("Upgrading stream files ...");
    Location oldLocation = locationFactory.create(cConf.get(Constants.Stream.BASE_DIR));
    Location newLocation = Locations.getParent(oldLocation).append(Constants.DEFAULT_NAMESPACE)
      .append(cConf.get(Constants.Stream.BASE_DIR));
    if (renameLocation(oldLocation, newLocation) != null) {
      LOG.info("Upgraded streams archives from {} to {}", oldLocation, newLocation);
    }
  }

  /**
   * Upgraded the logs path
   */
  private void upgradeLogs() throws IOException {
    LOG.info("Upgrading log files ...");
    String logBaseDir = cConf.get(LoggingConfiguration.LOG_BASE_DIR);
    upgradeLogsHelper(locationFactory.create(logBaseDir).append(Constants.Logging.SYSTEM_NAME),
                      Constants.SYSTEM_NAMESPACE);
    upgradeLogsHelper(locationFactory.create(logBaseDir).append(DEVELOPER_ACCOUNT), Constants.DEFAULT_NAMESPACE);
  }

  /**
   * Upgrades the log path through a temp location. This is required as the source and final destination are under same
   * directory.
   *
   * @param oldLogLocation the old {@link Location} of the log
   * @param namespace      the namespace under which this log should be stored
   * @throws IOException
   */
  private void upgradeLogsHelper(Location oldLogLocation, String namespace) throws IOException {
    Location tempLocation = locationFactory.create("temp");
    // create the new namespace locations
    Location newLogLocation = locationFactory.create(namespace).append(cConf.get(LoggingConfiguration.LOG_BASE_DIR));

    // move to new location
    renameLocation(oldLogLocation, tempLocation);
    renameLocation(tempLocation, newLogLocation);
  }
}
