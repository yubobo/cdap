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

package com.continuuity.shell;

import com.continuuity.client.config.ReactorClientConfig;
import com.continuuity.shell.command.CommandSet;
import com.continuuity.shell.command.ExitCommand;
import com.continuuity.shell.command.HelpCommand;
import com.continuuity.shell.command.VersionCommand;
import com.continuuity.shell.command.call.CallCommandSet;
import com.continuuity.shell.command.connect.ConnectCommand;
import com.continuuity.shell.command.create.CreateCommandSet;
import com.continuuity.shell.command.delete.DeleteCommandSet;
import com.continuuity.shell.command.deploy.DeployCommandSet;
import com.continuuity.shell.command.describe.DescribeCommandSet;
import com.continuuity.shell.command.execute.ExecuteQueryCommand;
import com.continuuity.shell.command.get.GetCommandSet;
import com.continuuity.shell.command.list.ListCommandSet;
import com.continuuity.shell.command.send.SendCommandSet;
import com.continuuity.shell.command.set.SetCommandSet;
import com.continuuity.shell.command.start.StartProgramCommandSet;
import com.continuuity.shell.command.stop.StopProgramCommandSet;
import com.continuuity.shell.command.truncate.TruncateCommandSet;
import com.continuuity.shell.exception.InvalidCommandException;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import jline.console.completer.Completer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

/**
 * Main class for Reactor shell.
 */
public class ReactorShellMain {

  private final CommandSet commands;
  private final ReactorShellConfig reactorShellConfig;
  private final HelpCommand helpCommand;
  private final ConsoleReader reader;

  public ReactorShellMain(final ReactorShellConfig reactorShellConfig) throws URISyntaxException, IOException {
    this.reader = new ConsoleReader();
    this.reactorShellConfig = reactorShellConfig;
    this.reactorShellConfig.addReactorHostChangeListener(new ReactorShellConfig.ReactorHostChangeListener() {
      @Override
      public void onReactorHostChanged(String newReactorHost) {
        reader.setPrompt(
          "reactor (" + reactorShellConfig.getReactorHost() + ":"
            + reactorShellConfig.getReactorConfig().getPort() + ")> ");
      }
    });
    this.helpCommand = new HelpCommand(new Supplier<CommandSet>() {
      @Override
      public CommandSet get() {
        return getCommands();
      }
    }, reactorShellConfig);

    Injector injector = Guice.createInjector(
      new AbstractModule() {
        @Override
        protected void configure() {
          bind(ReactorShellConfig.class).toInstance(reactorShellConfig);
          bind(ReactorClientConfig.class).toInstance(reactorShellConfig.getReactorConfig());
        }
      }
    );

    this.commands = CommandSet.builder(null)
      .addCommand(helpCommand)
      .addCommand(injector.getInstance(ConnectCommand.class))
      .addCommand(injector.getInstance(VersionCommand.class))
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
    this.reader.setPrompt("reactor ("
                            + reactorShellConfig.getReactorHost() + ":"
                            + reactorShellConfig.getReactorConfig().getPort() + ")> ");
    this.reader.setHandleUserInterrupt(true);

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
        String command = line.trim();
        String[] commandArgs = Iterables.toArray(Splitter.on(" ").split(command), String.class);
        try {
          processArgs(commandArgs, output);
        } catch (InvalidCommandException e) {
          output.println("Invalid command: " + command + " (enter 'help' to list all available commands)");
        } catch (Exception e) {
          output.println("Error: " + e.getMessage());
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

    ReactorShellConfig config = new ReactorShellConfig(reactorHost);
    ReactorShellMain shell = new ReactorShellMain(config);

    if (args.length == 0) {
      shell.startShellMode(System.out);
    } else {
      shell.processArgs(args, System.out);
    }
  }
}
