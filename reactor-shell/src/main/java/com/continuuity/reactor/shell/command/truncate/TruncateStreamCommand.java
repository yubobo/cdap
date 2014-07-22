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

package com.continuuity.reactor.shell.command.truncate;

import com.continuuity.reactor.client.ReactorStreamClient;
import com.continuuity.reactor.shell.CompleterFactory;
import com.continuuity.reactor.shell.command.AbstractCommand;
import com.continuuity.reactor.shell.completer.Completable;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.util.List;

/**
 * Truncates a stream.
 */
public class TruncateStreamCommand extends AbstractCommand implements Completable {

  private final ReactorStreamClient streamClient;
  private final CompleterFactory completerFactory;

  protected TruncateStreamCommand(CompleterFactory completerFactory, ReactorStreamClient streamClient) {
    super("stream", null, "Truncates a stream");
    this.completerFactory = completerFactory;
    this.streamClient = streamClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String streamId = args[0];
    streamClient.truncate(streamId);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, completerFactory.getStreamIdCompleter()));
  }
}
