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

package com.continuuity.shell.command.list;

import com.continuuity.reactor.client.ReactorDatasetClient;
import com.continuuity.reactor.metadata.DatasetSpecification;
import com.continuuity.shell.command.AbstractCommand;
import com.continuuity.shell.util.AsciiTable;
import com.continuuity.shell.util.RowMaker;

import java.io.PrintStream;
import java.util.List;
import javax.inject.Inject;

/**
 * Lists datasets.
 */
public class ListDatasetInstancesCommand extends AbstractCommand {

  private final ReactorDatasetClient datasetClient;

  @Inject
  public ListDatasetInstancesCommand(ReactorDatasetClient datasetClient) {
    super("instances", null, "Lists all datasets");
    this.datasetClient = datasetClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    List<DatasetSpecification> datasetMetas = datasetClient.list();

    new AsciiTable<DatasetSpecification>(
      new String[]{"name", "type"}, datasetMetas,
      new RowMaker<DatasetSpecification>() {
        @Override
        public Object[] makeRow(DatasetSpecification object) {
          return new Object[] { object.getName(), object.getType() };
        }
      }).print(output);
  }
}
