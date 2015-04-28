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

package co.cask.cdap.examples.countrandom;

import co.cask.cdap.api.dataset.DatasetAdmin;
import co.cask.cdap.api.dataset.DatasetContext;
import co.cask.cdap.api.dataset.DatasetSpecification;
import org.apache.twill.filesystem.LocalLocationFactory;
import org.apache.twill.filesystem.Location;

import java.io.IOException;

/**
 * Administration for file sets.
 */
public class FileSetAdmin implements DatasetAdmin {

  private final Location baseLocation;

  public FileSetAdmin(DatasetContext datasetContext,
                      DatasetSpecification spec) throws IOException {
    this.baseLocation = new LocalLocationFactory().create(CustomFileSetDataset.determineBasePath(spec));
  }

  @Override
  public boolean exists() throws IOException {
    return baseLocation.isDirectory();
  }

  @Override
  public void create() throws IOException {
    baseLocation.mkdirs();
  }

  @Override
  public void drop() throws IOException {
    baseLocation.delete(true);
  }

  @Override
  public void truncate() throws IOException {
    drop();
    create();
  }

  @Override
  public void upgrade() throws IOException {
    // nothing to do
  }

  @Override
  public void close() throws IOException {
    // nothing to do
  }
}
