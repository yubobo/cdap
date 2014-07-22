/*
 * Copyright 2012-2014 Continuuity, Inc.
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

package com.continuuity.reactor.shell.command.describe;

import com.continuuity.reactor.client.ReactorDatasetTypeClient;
import com.continuuity.reactor.metadata.DatasetTypeMeta;
import com.continuuity.reactor.shell.CompleterFactory;
import com.continuuity.reactor.shell.command.AbstractCommand;
import com.continuuity.reactor.shell.completer.Completable;
import com.continuuity.reactor.shell.completer.reactor.DatasetTypeNameCompleter;
import com.continuuity.reactor.shell.util.AsciiTable;
import com.continuuity.reactor.shell.util.RowMaker;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.util.List;

/**
 * Shows information about a dataset type.
 */
public class DescribeDatasetTypeCommand extends AbstractCommand implements Completable {

  private final ReactorDatasetTypeClient datasetTypeClient;
  private final CompleterFactory completerFactory;

  public DescribeDatasetTypeCommand(CompleterFactory completerFactory, ReactorDatasetTypeClient datasetTypeClient) {
    // TODO: dataset type
    super("type", "<type-name>", "Shows information about a dataset type");
    this.completerFactory = completerFactory;
    this.datasetTypeClient = datasetTypeClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String typeName = args[0];
    DatasetTypeMeta datasetTypeMeta = datasetTypeClient.get(typeName);

    new AsciiTable<DatasetTypeMeta>(
      new String[] { "name", "modules" }, Lists.newArrayList(datasetTypeMeta),
      new RowMaker<DatasetTypeMeta>() {
        @Override
        public Object[] makeRow(DatasetTypeMeta object) {
          return new Object[] { object.getName(), Joiner.on(", ").join(object.getModules()) };
        }
      }).print(output);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, completerFactory.getDatasetTypeNameCompleter()));
  }
}
