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

package com.continuuity.shell.command.connect;

import com.continuuity.shell.ReactorShellConfig;
import com.continuuity.shell.command.AbstractCommand;
import com.continuuity.shell.util.SocketUtil;

import java.io.PrintStream;
import javax.inject.Inject;

/**
 * Connects to a Reactor instance.
 */
public class ConnectCommand extends AbstractCommand {

  private final ReactorShellConfig shellConfig;

  @Inject
  public ConnectCommand(ReactorShellConfig shellConfig) {
    super("connect", "<reactor-hostname>", "Connects to a Reactor instance");
    this.shellConfig = shellConfig;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String hostname = args[0];
    int port = shellConfig.getReactorConfig().getPort();

    boolean available = SocketUtil.isAvailable(hostname, port);
    if (available) {
      shellConfig.setReactorHost(hostname);
    } else {
      output.println(String.format("Host %s on port %d could not be reached", hostname, port));
    }
  }
}
