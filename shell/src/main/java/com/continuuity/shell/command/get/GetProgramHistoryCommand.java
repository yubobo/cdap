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

package com.continuuity.shell.command.get;

import com.continuuity.reactor.client.ReactorProgramClient;
import com.continuuity.reactor.metadata.RunRecord;
import com.continuuity.shell.ProgramElementType;
import com.continuuity.shell.ProgramIdCompleterFactory;
import com.continuuity.shell.command.AbstractCommand;
import com.continuuity.shell.completer.Completable;
import com.continuuity.shell.util.AsciiTable;
import com.continuuity.shell.util.RowMaker;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.util.List;

/**
 * Gets the run history of a program.
 */
public class GetProgramHistoryCommand extends AbstractCommand implements Completable {

  private final ReactorProgramClient programClient;
  private final ProgramIdCompleterFactory completerFactory;
  private final ProgramElementType programElementType;

  protected GetProgramHistoryCommand(ProgramElementType programElementType,
                                     ProgramIdCompleterFactory completerFactory,
                                     ReactorProgramClient programClient) {
    super(programElementType.getName(), "<app-id>.<program-id>",
          "Gets the run history of a " + programElementType.getName());
    this.programElementType = programElementType;
    this.completerFactory = completerFactory;
    this.programClient = programClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String[] programIdParts = args[0].split("\\.");
    String appId = programIdParts[0];

    List<RunRecord> history;
    if (programElementType == ProgramElementType.RUNNABLE) {
      String serviceId = programIdParts[1];
      String runnableId = programIdParts[2];
      history = programClient.getServiceRunnableHistory(appId, serviceId, runnableId);
    } else if (programElementType.getProgramType() != null) {
      String programId = programIdParts[1];
      history = programClient.getProgramHistory(appId, programElementType.getProgramType(), programId);
    } else {
      throw new IllegalArgumentException("Unrecognized program element type for history: " + programElementType);
    }

    new AsciiTable<RunRecord>(
      new String[] { "pid", "end status", "start", "stop" },
      history,
      new RowMaker<RunRecord>() {
        @Override
        public Object[] makeRow(RunRecord object) {
          return new Object[] { object.getPid(), object.getEndStatus(), object.getStartTs(), object.getStopTs() };
        }
      }
    ).print(output);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, completerFactory.getProgramIdCompleter(programElementType)));
  }
}
