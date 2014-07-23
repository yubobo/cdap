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

package com.continuuity.reactor.shell.command.delete;

import com.continuuity.reactor.client.ReactorAppClient;
import com.continuuity.reactor.client.ReactorDatasetClient;
import com.continuuity.reactor.client.ReactorDatasetModuleClient;
import com.continuuity.reactor.shell.command.Command;
import com.continuuity.reactor.shell.command.CommandSet;
import com.continuuity.reactor.shell.completer.reactor.AppIdCompleter;
import com.continuuity.reactor.shell.completer.reactor.DatasetModuleNameCompleter;
import com.continuuity.reactor.shell.completer.reactor.DatasetNameCompleter;
import com.google.common.collect.Lists;

import javax.inject.Inject;

/**
 * Contains commands for deleting stuff.
 */
public class DeleteCommandSet extends CommandSet {

  @Inject
  public DeleteCommandSet(AppIdCompleter appIdCompleter, DatasetNameCompleter datasetNameCompleter,
                          DatasetModuleNameCompleter datasetModuleNameCompleter,
                          ReactorAppClient appClient, ReactorDatasetClient datasetClient,
                          ReactorDatasetModuleClient datasetModuleClient) {
    super("delete", Lists.<Command>newArrayList(
      new DeleteAppCommand(appIdCompleter, appClient),
      new DeleteDatasetCommand(datasetNameCompleter, datasetClient),
      new DeleteDatasetModuleCommand(datasetModuleNameCompleter, datasetModuleClient)
    ));
  }
}
