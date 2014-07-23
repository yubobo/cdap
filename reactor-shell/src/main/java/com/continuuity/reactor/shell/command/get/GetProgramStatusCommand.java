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

package com.continuuity.reactor.shell.command.get;

import com.continuuity.reactor.client.ReactorProgramClient;
import com.continuuity.reactor.shell.ProgramElementType;
import com.continuuity.reactor.shell.ProgramIdCompleterFactory;
import com.continuuity.reactor.shell.command.AbstractCommand;
import com.continuuity.reactor.shell.completer.Completable;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.util.List;

/**
 * Gets the status of a program.
 */
public class GetProgramStatusCommand extends AbstractCommand implements Completable {

  private final ReactorProgramClient programClient;
  private final ProgramIdCompleterFactory completerFactory;
  private final ProgramElementType programElementType;

  protected GetProgramStatusCommand(ProgramElementType programElementType,
                                    ProgramIdCompleterFactory completerFactory,
                                    ReactorProgramClient programClient) {
    super(programElementType.getName(), "<app-id>.<program-id>",
          "Gets the status of a " + programElementType.getName());
    this.programElementType = programElementType;
    this.completerFactory = completerFactory;
    this.programClient = programClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String[] programIdParts = args[0].split("\\.");
    String appId = programIdParts[0];
    String programId = programIdParts[1];

    String status = programClient.getStatus(appId, programElementType.getProgramType(), programId);
    output.println(status);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, completerFactory.getProgramIdCompleter(programElementType)));
  }
}
