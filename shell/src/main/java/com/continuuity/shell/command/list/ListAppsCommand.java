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
import com.continuuity.proto.ApplicationRecord;
import com.continuuity.shell.command.AbstractCommand;
import com.continuuity.shell.util.AsciiTable;
import com.continuuity.shell.util.RowMaker;

import java.io.PrintStream;
import javax.inject.Inject;

/**
 * Lists all applications.
 */
public class ListAppsCommand extends AbstractCommand {

  private final ApplicationClient appClient;

  @Inject
  public ListAppsCommand(ApplicationClient appClient) {
    super("apps", null, "Lists all applications");
    this.appClient = appClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    new AsciiTable<ApplicationRecord>(
      new String[] { "id", "description" },
      appClient.list(),
      new RowMaker<ApplicationRecord>() {
        @Override
        public Object[] makeRow(ApplicationRecord object) {
          return new Object[] { object.getId(), object.getDescription() };
        }
      }
    ).print(output);
  }
}
