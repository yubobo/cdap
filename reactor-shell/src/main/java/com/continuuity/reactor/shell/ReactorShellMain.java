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
public class ReactorShellMain {

  private final CommandSet commands;
  private final String reactorHost;

  private final HelpCommand helpCommand;

  private final ReactorClientConfig reactorConfig;

  /**
   * @param reactorHost Hostname of the Reactor instance to interact with (e.g. "example.com")
   * @throws URISyntaxException
   */
  public ReactorShellMain(String reactorHost) throws URISyntaxException {
    this.reactorHost = Objects.firstNonNull(reactorHost, "localhost");
    this.reactorConfig = new ReactorClientConfig(reactorHost);
    this.helpCommand = new HelpCommand(new Supplier<CommandSet>() {
      @Override
      public CommandSet get() {
        return getCommands();
      }
    });

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
      .addCommand(injector.getInstance(ExitCommand.class))
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

  /**
   * Starts shell mode, which provides a shell to enter multiple commands and use autocompletion.
   *
   * @param output {@link PrintStream} to write to
   * @throws Exception
   */
  public void startShellMode(PrintStream output) throws Exception {
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
          processArgs(commandArgs, output);
        } catch (InvalidCommandException e) {
          output.println(e.getMessage() + "\n");
          helpCommand.process(null, output);
        } catch (Exception e) {
          e.printStackTrace();
        }
        output.println();
      }
    }
  }

  /**
   * Processes a command and writes to the provided output
   * @param args the tokens of the command string (e.g. ["start", "flow", "SomeApp.SomeFlow"])
   * @throws Exception
   */
  public void processArgs(String[] args, PrintStream output) throws Exception {
    commands.process(args, output);
  }

  private CommandSet getCommands() {
    return commands;
  }

  public static void main(String[] args) throws Exception {
    String reactorHost = System.getenv("REACTOR_HOST");
    if (reactorHost == null) {
      reactorHost = "localhost";
    }

    ReactorShellMain shell = new ReactorShellMain(reactorHost);

    if (args.length == 0) {
      shell.startShellMode(System.out);
    } else {
      shell.processArgs(args, System.out);
    }
  }
}
