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

package com.continuuity.reactor.shell;

import com.continuuity.reactor.client.config.ReactorClientConfig;
import com.continuuity.reactor.client.util.RestClient;
import com.continuuity.reactor.shell.command.CommandSet;
import com.continuuity.reactor.shell.command.ExitCommand;
import com.continuuity.reactor.shell.command.HelpCommand;
import com.continuuity.reactor.shell.command.call.CallCommandSet;
import com.continuuity.reactor.shell.command.create.CreateCommandSet;
import com.continuuity.reactor.shell.command.delete.DeleteCommandSet;
import com.continuuity.reactor.shell.command.deploy.DeployCommandSet;
import com.continuuity.reactor.shell.command.describe.DescribeCommandSet;
import com.continuuity.reactor.shell.command.execute.ExecuteQueryCommand;
import com.continuuity.reactor.shell.command.get.GetCommandSet;
import com.continuuity.reactor.shell.command.list.ListCommandSet;
import com.continuuity.reactor.shell.command.send.SendCommandSet;
import com.continuuity.reactor.shell.command.set.SetCommandSet;
import com.continuuity.reactor.shell.command.start.StartProgramCommandSet;
import com.continuuity.reactor.shell.command.stop.StopProgramCommandSet;
import com.continuuity.reactor.shell.command.truncate.TruncateCommandSet;
import com.continuuity.reactor.shell.exception.InvalidCommandException;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.net.URISyntaxException;

/**
 * Main class for Reactor shell.
 */
// TODO: change program-related to "start/stop/status/history <element-type>.<app-id>.<element-id>"
// TODO: describe flowlet <element-type>.<app-id>.<element-id> (gets the # of instances + other info)
// TODO: show flowlet instances <..>
public class ReactorShellMain {

  private final PrintStream output;
  private final CommandSet commands;
  private final String reactorHost;
  private final RestClient restClient;

  private final HelpCommand helpCommand;
  private final ExitCommand exitCommand;

  private final ReactorClientConfig reactorConfig;

  public ReactorShellMain(String reactorHost, PrintStream output) throws URISyntaxException {
    this.reactorHost = Objects.firstNonNull(reactorHost, "localhost");
    this.reactorConfig = new ReactorClientConfig(reactorHost);
    this.restClient = RestClient.create(reactorConfig);
    this.output = output;
    this.helpCommand = new HelpCommand(new Supplier<CommandSet>() {
      @Override
      public CommandSet get() {
        return getCommands();
      }
    });
    this.exitCommand = new ExitCommand();

    Injector injector = Guice.createInjector(
      new AbstractModule() {
        @Override
        protected void configure() {
          bind(ReactorClientConfig.class).toInstance(reactorConfig);
        }
      }
    );

    this.commands = CommandSet.builder(null)
      .addCommand(helpCommand)
      .addCommand(exitCommand)
      .addCommand(injector.getInstance(CallCommandSet.class))
      .addCommand(injector.getInstance(CreateCommandSet.class))
      .addCommand(injector.getInstance(DeleteCommandSet.class))
      .addCommand(injector.getInstance(DeployCommandSet.class))
      .addCommand(injector.getInstance(DescribeCommandSet.class))
      .addCommand(injector.getInstance(ExecuteQueryCommand.class))
      .addCommand(injector.getInstance(GetCommandSet.class))
      .addCommand(injector.getInstance(ListCommandSet.class))
      .addCommand(injector.getInstance(SendCommandSet.class))
      .addCommand(injector.getInstance(SetCommandSet.class))
      .addCommand(injector.getInstance(StartProgramCommandSet.class))
      .addCommand(injector.getInstance(StopProgramCommandSet.class))
      .addCommand(injector.getInstance(TruncateCommandSet.class))
      .build();
  }

  public void parse(String[] args) throws Exception {
    if (args.length == 0) {
      // reactor shell mode
      final ConsoleReader reader = new ConsoleReader();
      reader.setPrompt("reactor (" + reactorHost + ")> ");
      reader.setHandleUserInterrupt(true);

      for (Completer completer : commands.getCompleters(null)) {
        reader.addCompleter(completer);
      }

      while (true) {
        String line;

        try {
          line = reader.readLine();
        } catch (UserInterruptException e) {
          continue;
        }

        if (line == null) {
          output.println();
          break;
        }

        if (line.length() > 0) {
          String[] commandArgs = Iterables.toArray(Splitter.on(" ").split(line.trim()), String.class);
          try {
            parseArgs(commandArgs);
          } catch (InvalidCommandException e) {
            output.println(e.getMessage() + "\n");
            helpCommand.process(null, output);
          } catch (Exception e) {
            e.printStackTrace();
          }
          output.println();
        }
      }
    } else {
      // other mode
      parseArgs(args);
    }
  }

  public CommandSet getCommands() {
    return commands;
  }

  private void parseArgs(String[] args) throws Exception {
    commands.process(args, output);
  }

  public static void main(String[] args) throws Exception {
    String reactorHost = System.getenv("REACTOR_HOST");
    if (reactorHost == null) {
      reactorHost = "localhost";
    }
    new ReactorShellMain(reactorHost, System.out).parse(args);
  }
}
