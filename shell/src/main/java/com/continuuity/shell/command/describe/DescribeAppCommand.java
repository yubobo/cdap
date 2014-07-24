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

package com.continuuity.shell.command.describe;

import com.continuuity.client.ApplicationClient;
import com.continuuity.proto.ProgramRecord;
import com.continuuity.proto.ProgramType;
import com.continuuity.shell.ElementType;
import com.continuuity.shell.command.AbstractCommand;
import com.continuuity.shell.completer.Completable;
import com.continuuity.shell.completer.reactor.AppIdCompleter;
import com.continuuity.shell.util.AsciiTable;
import com.continuuity.shell.util.RowMaker;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Shows detailed information about an application.
 */
public class DescribeAppCommand extends AbstractCommand implements Completable {

  private final ApplicationClient applicationClient;
  private final AppIdCompleter completer;

  @Inject
  public DescribeAppCommand(AppIdCompleter completer, ApplicationClient applicationClient) {
    super("app", "<app-id>", "Shows detailed information about an " + ElementType.APP.getPrettyName());
    this.completer = completer;
    this.applicationClient = applicationClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String appId = args[0];
    Map<ProgramType, List<ProgramRecord>> programs = applicationClient.listPrograms(appId);
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
