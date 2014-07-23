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

import com.continuuity.client.ApplicationClient;
import com.continuuity.proto.ProgramRecord;
import com.continuuity.proto.ProgramType;
import com.continuuity.shell.command.AbstractCommand;
import com.continuuity.shell.util.AsciiTable;
import com.continuuity.shell.util.RowMaker;
import com.google.common.collect.Lists;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Lists all programs.
 */
public class ListAllProgramsCommand extends AbstractCommand {

  private final ApplicationClient appClient;

  @Inject
  public ListAllProgramsCommand(ApplicationClient appClient) {
    super("programs", null, "Lists all programs");
    this.appClient = appClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    Map<ProgramType, List<ProgramRecord>> allPrograms = appClient.listAllPrograms();
    List<ProgramRecord> allProgramsList = Lists.newArrayList();
    for (List<ProgramRecord> subList : allPrograms.values()) {
      allProgramsList.addAll(subList);
    }

    new AsciiTable<ProgramRecord>(
      new String[] { "type", "app", "id", "description" },
      allProgramsList,
      new RowMaker<ProgramRecord>() {
        @Override
        public Object[] makeRow(ProgramRecord object) {
          return new Object[] { object.getType().getCategoryName(), object.getApp(),
            object.getId(), object.getDescription() };
        }
      }
    ).print(output);
  }
}
