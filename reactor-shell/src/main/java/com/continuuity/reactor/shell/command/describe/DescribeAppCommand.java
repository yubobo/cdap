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

import com.continuuity.reactor.client.ReactorAppClient;
import com.continuuity.reactor.metadata.ProgramRecord;
import com.continuuity.reactor.metadata.ProgramType;
import com.continuuity.reactor.shell.command.AbstractCommand;
import com.continuuity.reactor.shell.completer.Completable;
import com.continuuity.reactor.shell.completer.reactor.AppIdCompleter;
import com.continuuity.reactor.shell.util.AsciiTable;
import com.continuuity.reactor.shell.util.RowMaker;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Shows detailed information about an application.
 */
public class DescribeAppCommand extends AbstractCommand implements Completable {

  private final ReactorAppClient reactorAppClient;
  private final AppIdCompleter completer;

  public DescribeAppCommand(AppIdCompleter completer, ReactorAppClient reactorAppClient) {
    super("app", "<app-id>", "Shows detailed information about an application");
    this.completer = completer;
    this.reactorAppClient = reactorAppClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String appId = args[0];
    Map<ProgramType, List<ProgramRecord>> programs = reactorAppClient.listPrograms(appId);
    List<ProgramRecord> programsList = Lists.newArrayList();
    for (List<ProgramRecord> subList : programs.values()) {
      programsList.addAll(subList);
    }

    new AsciiTable<ProgramRecord>(
      new String[] { "app", "type", "id", "description" },
      programsList,
      new RowMaker<ProgramRecord>() {
        @Override
        public Object[] makeRow(ProgramRecord object) {
          return new Object[] { object.getApp(), object.getType().getCategoryName(),
            object.getId(), object.getDescription() };
        }
      }
    ).print(output);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, completer));
  }
}
