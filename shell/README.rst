# Shell Client

## Introduction

The Shell Client provides a way to interact with Reactor using a shell, similar to hbase shell or bash.

## How to Build

```
mvn clean package -DskipTests -pl shell -am
```

The shell executable is located at `shell/target/reactor`.

## Usage

The Shell Client may be used in two ways: 1) interactive mode and 2) non-interactive mode

### Interactive Mode

To run the Shell Client in interactive mode, run the `reactor` executable with no arguments from the terminal.
The executable should bring you into a shell, with the following prompt:

```
reactor (localhost)>
```

This indicates that the Shell Client is currently set to interact with the Reactor instance at localhost.
To interact with a different Reactor instance, you may modify the environment variable `REACTOR_HOST`.
For example, with `REACTOR_HOST` set to `example.com`, the shell client would be interacting with
a Reactor instance at `example.com` port `10000`.

To list all of the available commands, you may enter `help`.

### Non-Interactive Mode

To run the shell in executable mode, run the `reactor` executable and provide the command you wish
to execute as the argument. For example, to list all applications currently deployed to Reactor,
you may run `reactor list apps`.

Available Commands
==================

Below is the list of all available commands::

  help: Prints this helper text
  exit: Exits the shell
  call procedure <app-id>.<procedure-id> <method-id> <parameters-map>: Calls a procedure
  create stream <new-stream-id>: Creates a stream
  create dataset instance <type-name> <new-dataset-name>: Creates a dataset
  delete app <app-id>: Deletes an application
  delete dataset instance <dataset-name>: Deletes a dataset
  delete dataset module <module-name>: Deletes a dataset module
  deploy app <app-jar-file>: Deploys an application
  deploy dataset module <module-jar-file> <module-name> <module-jar-classname>: Deploys a dataset module
  describe app <app-id>: Shows detailed information about an application
  describe dataset module <module-name>: Shows information about a dataset module
  describe dataset type <type-name>: Shows information about a dataset type
  execute <query>: Executes a dataset query
  get history flow <app-id>.<program-id>: Gets the run history of a flow
  get history workflow <app-id>.<program-id>: Gets the run history of a workflow
  get history procedure <app-id>.<program-id>: Gets the run history of a procedure
  get history runnable <app-id>.<program-id>: Gets the run history of a runnable
  get history mapreduce <app-id>.<program-id>: Gets the run history of a mapreduce
  get instances flowlet <app-id>.<program-id>: Gets the instances of a flowlet
  get instances procedure <app-id>.<program-id>: Gets the instances of a procedure
  get instances runnable <app-id>.<program-id>: Gets the instances of a runnable
  get live flow <app-id>.<program-id>: Gets the live info of a flow
  get live procedure <app-id>.<program-id>: Gets the live info of a procedure
  get logs flow <app-id>.<program-id> [<start-time> <end-time>]: Gets the logs of a flow
  get logs procedure <app-id>.<program-id> [<start-time> <end-time>]: Gets the logs of a procedure
  get logs runnable <app-id>.<program-id> [<start-time> <end-time>]: Gets the logs of a runnable
  get logs mapreduce <app-id>.<program-id> [<start-time> <end-time>]: Gets the logs of a mapreduce
  get status flow <app-id>.<program-id>: Gets the status of a flow
  get status workflow <app-id>.<program-id>: Gets the status of a workflow
  get status procedure <app-id>.<program-id>: Gets the status of a procedure
  get status service <app-id>.<program-id>: Gets the status of a service
  get status mapreduce <app-id>.<program-id>: Gets the status of a mapreduce
  list apps: Lists all applications
  list programs: Lists all programs
  list flows: Lists flows
  list mapreduce: Lists mapreduce
  list procedures: Lists procedures
  list workflows: Lists workflows
  list dataset instances: Lists all datasets
  list dataset modules: Lists dataset modules
  list dataset types: Lists dataset types
  list streams: Lists streams
  send stream <stream-id> <stream-event>: Sends an event to a stream
  set instances flowlet <program-id> <num-instances>: Sets the instances of a flowlet
  set instances procedure <program-id> <num-instances>: Sets the instances of a procedure
  set instances runnable <program-id> <num-instances>: Sets the instances of a runnable
  set stream ttl <stream-id> <ttl-in-seconds>: Sets the Time-to-Live (TTL) of a stream
  start flow <program-id>: Starts a flow
  start workflow <program-id>: Starts a workflow
  start procedure <program-id>: Starts a procedure
  start service <program-id>: Starts a service
  start mapreduce <program-id>: Starts a mapreduce
  stop flow <program-id>: Stops a flow
  stop workflow <program-id>: Stops a workflow
  stop procedure <program-id>: Stops a procedure
  stop service <program-id>: Stops a service
  stop mapreduce <program-id>: Stops a mapreduce
  truncate dataset instance: Truncates a dataset
  truncate stream: Truncates a stream
