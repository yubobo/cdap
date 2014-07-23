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

package com.continuuity.reactor.shell.command.stop;

import com.continuuity.reactor.client.ReactorProgramClient;
import com.continuuity.reactor.shell.ProgramElementType;
import com.continuuity.reactor.shell.ProgramIdCompleterFactory;
import com.continuuity.reactor.shell.command.Command;
import com.continuuity.reactor.shell.command.CommandSet;
import com.google.common.collect.Lists;

import java.util.List;
import javax.inject.Inject;

/**
 * Contains commands for getting the number of instances a program is running on.
 */
public class StopProgramCommandSet extends CommandSet {

  @Inject
  public StopProgramCommandSet(ProgramIdCompleterFactory programIdCompleterFactory,
                               ReactorProgramClient programClient) {
    super("stop", generateCommands(programIdCompleterFactory, programClient));
  }

  public static List<Command> generateCommands(ProgramIdCompleterFactory programIdCompleterFactory,
                                               ReactorProgramClient programClient) {
    List<Command> commands = Lists.newArrayList();
    for (ProgramElementType programElementType : ProgramElementType.values()) {
      if (programElementType.canStartStop()) {
        commands.add(new StopProgramCommand(programElementType, programIdCompleterFactory, programClient));
      }
    }
    return commands;
  }
}
