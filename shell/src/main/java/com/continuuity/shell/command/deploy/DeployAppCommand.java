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

package com.continuuity.shell.command.deploy;

import com.continuuity.client.ApplicationClient;
import com.continuuity.shell.ElementType;
import com.continuuity.shell.command.AbstractCommand;
import com.continuuity.shell.completer.Completable;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import javax.inject.Inject;

/**
 * Deploys an application.
 */
public class DeployAppCommand extends AbstractCommand implements Completable {

  private final ApplicationClient applicationClient;

  @Inject
  public DeployAppCommand(ApplicationClient applicationClient) {
    super("app", "<app-jar-file>", "Deploys an " + ElementType.APP.getPrettyName());
    this.applicationClient = applicationClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    File file = new File(args[0]);
    Preconditions.checkArgument(file.exists(), "File " + file.getAbsolutePath() + " does not exist");
    Preconditions.checkArgument(file.canRead(), "File " + file.getAbsolutePath() + " is not readable");
    applicationClient.deploy(file);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, new FileNameCompleter()));
  }
}
