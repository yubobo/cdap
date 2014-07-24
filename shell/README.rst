==============
 Shell Client
==============

Introduction
============

The Shell Client provides methods to interact with Reactor using a shell, similar to HBase shell or ``bash``.

How to Build
============

::

  mvn clean package -DskipTests -pl shell -am

The shell executable is located at ``shell/target/reactor``.

Usage
=====

The Shell Client may be used in two ways: interactive mode and non-interactive mode

Interactive Mode
================

To run the Shell Client in interactive mode, run the ``reactor`` executable with no arguments from the terminal.
The executable should bring you into a shell, with this prompt::

  reactor (localhost:10000)>

This indicates that the Shell Client is currently set to interact with the Reactor instance at ``localhost``.
To interact with a different Reactor instance, modify the environment variable ``REACTOR_HOST``
and start the Shell Client again.

For example, with ``REACTOR_HOST`` set to ``example.com``, the Shell Client would be interacting with
a Reactor instance at ``example.com` port ``10000``::

  reactor (example.com:10000)>

To list all of the available commands, enter ``help``::

  reactor (localhost:10000)> help

Non-Interactive Mode
====================

To run the Shell Client in executable mode, run the ``reactor`` executable, passing the command you want executed
as the argument. For example, to list all applications currently deployed to Reactor, execute this::

  reactor list apps

Available Commands
==================

Here are all of the available commands::

- ``help``: Prints this helper text
- ``version``: Prints the version
- ``exit``: Exits the shell
- ``call procedure <app-id>.<procedure-id> <method-id> <parameters-map>``: Calls a Procedure, passing in
the parameters as a JSON String map
- ``create stream <new-stream-id>``: Creates a Stream
- ``create dataset instance <type-name> <new-dataset-name>``: Creates a Dataset
- ``delete app <app-id>``: Deletes an application
- ``delete dataset instance <dataset-name>``: Deletes a Dataset
- ``delete dataset module <module-name>``: Deletes a Dataset module
- ``deploy app <app-jar-file>``: Deploys an application
- ``deploy dataset module <module-jar-file> <module-name> <module-jar-classname>``: Deploys a Dataset module
- ``describe app <app-id>``: Shows detailed information about an application
- ``describe dataset module <module-name>``: Shows information about a Dataset module
- ``describe dataset type <type-name>``: Shows information about a Dataset type
- ``execute <query>``: Executes a Dataset query
- ``get history flow <app-id>.<program-id>``: Gets the run history of a Flow
- ``get history workflow <app-id>.<program-id>``: Gets the run history of a Workflow
- ``get history procedure <app-id>.<program-id>``: Gets the run history of a Procedure
- ``get history runnable <app-id>.<program-id>``: Gets the run history of a Runnable
- ``get history mapreduce <app-id>.<program-id>``: Gets the run history of a MapReduce job
- ``get instances flowlet <app-id>.<program-id>``: Gets the instances of a Flowlet
- ``get instances procedure <app-id>.<program-id>``: Gets the instances of a Procedure
- ``get instances runnable <app-id>.<program-id>``: Gets the instances of a Runnable
- ``get live flow <app-id>.<program-id>``: Gets the live info of a Flow
- ``get live procedure <app-id>.<program-id>``: Gets the live info of a Procedure
- ``get logs flow <app-id>.<program-id> [<start-time> <end-time>]``: Gets the logs of a Flow
- ``get logs procedure <app-id>.<program-id> [<start-time> <end-time>]``: Gets the logs of a Procedure
- ``get logs runnable <app-id>.<program-id> [<start-time> <end-time>]``: Gets the logs of a Runnable
- ``get logs mapreduce <app-id>.<program-id> [<start-time> <end-time>]``: Gets the logs of a MapReduce job
- ``get status flow <app-id>.<program-id>``: Gets the status of a Flow
- ``get status workflow <app-id>.<program-id>``: Gets the status of a Workflow
- ``get status procedure <app-id>.<program-id>``: Gets the status of a Procedure
- ``get status service <app-id>.<program-id>``: Gets the status of a Service
- ``get status mapreduce <app-id>.<program-id>``: Gets the status of a MapReduce job
- ``list apps``: Lists all applications
- ``list programs``: Lists all programs
- ``list flows``: Lists Flows
- ``list mapreduce``: Lists MapReduce jobs
- ``list procedures``: Lists Procedures
- ``list workflows``: Lists Workflows
- ``list dataset instances``: Lists all Datasets
- ``list dataset modules``: Lists Dataset modules
- ``list dataset types``: Lists Dataset types
- ``list streams``: Lists Streams
- ``send stream <stream-id> <stream-event>``: Sends an event to a Stream
- ``set instances flowlet <program-id> <num-instances>``: Sets the instances of a Flowlet
- ``set instances procedure <program-id> <num-instances>``: Sets the instances of a Procedure
- ``set instances runnable <program-id> <num-instances>``: Sets the instances of a Runnable
- ``set stream ttl <stream-id> <ttl-in-seconds>``: Sets the Time-to-Live (TTL) of a Stream
- ``start flow <program-id>``: Starts a Flow
- ``start workflow <program-id>``: Starts a Workflow
- ``start procedure <program-id>``: Starts a Procedure
- ``start service <program-id>``: Starts a Service
- ``start mapreduce <program-id>``: Starts a MapReduce job
- ``stop flow <program-id>``: Stops a Flow
- ``stop workflow <program-id>``: Stops a Workflow
- ``stop procedure <program-id>``: Stops a Procedure
- ``stop service <program-id>``: Stops a Service
- ``stop mapreduce <program-id>``: Stops a MapReduce job
- ``truncate dataset instance``: Truncates a Dataset
- ``truncate stream``: Truncates a Stream
