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

import com.continuuity.client.StreamClient;
import com.continuuity.proto.StreamRecord;
import com.continuuity.shell.command.AbstractCommand;
import com.continuuity.shell.util.AsciiTable;
import com.continuuity.shell.util.RowMaker;

import java.io.PrintStream;
import javax.inject.Inject;

/**
 * Lists streams.
 */
public class ListStreamsCommand extends AbstractCommand {

  private final StreamClient streamClient;

  @Inject
  public ListStreamsCommand(StreamClient streamClient) {
    super("streams", null, "Lists streams");
    this.streamClient = streamClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    new AsciiTable<StreamRecord>(
      new String[] { "name" },
      streamClient.list(),
      new RowMaker<StreamRecord>() {
        @Override
        public Object[] makeRow(StreamRecord object) {
          return new String[]{object.getId()};
        }
      }).print(output);
  }
}
