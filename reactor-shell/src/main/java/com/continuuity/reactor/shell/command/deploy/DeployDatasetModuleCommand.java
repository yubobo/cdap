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

package com.continuuity.reactor.shell.command.deploy;

import com.continuuity.reactor.client.ReactorDatasetModuleClient;
import com.continuuity.reactor.shell.command.AbstractCommand;
import com.continuuity.reactor.shell.completer.Completable;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import javax.inject.Inject;

/**
 * Deploys a dataset module.
 */
public class DeployDatasetModuleCommand extends AbstractCommand implements Completable {

  private final ReactorDatasetModuleClient datasetModuleClient;

  @Inject
  public DeployDatasetModuleCommand(ReactorDatasetModuleClient datasetModuleClient) {
    super("module", "<module-jar-file> <module-name> <module-jar-classname>", "Deploys a dataset module");
    this.datasetModuleClient = datasetModuleClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    File moduleJarFile = new File(args[0]);
    Preconditions.checkArgument(moduleJarFile.exists(),
                                "Module jar file '" + moduleJarFile.getAbsolutePath() + "' does not exist");
    Preconditions.checkArgument(moduleJarFile.canRead());
    String moduleName = args[1];
    String moduleJarClassname = args[2];

    datasetModuleClient.add(moduleName, moduleJarClassname, moduleJarFile);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, new FileNameCompleter()));
  }
}
