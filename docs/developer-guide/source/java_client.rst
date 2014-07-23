=================
 Java Client API
=================

Introduction
============

The Java Client API provides a way to interact with Reactor using Java code.

Maven Dependency
================

To use the Java Client API in your project, add the following Maven dependency::

  <dependency>
    <groupId>com.continuuity</groupId>
    <artifactId>client</artifactId>
    <version>${reactor.version}</version>
  </dependency>

Components
==========

The Java Client API allows you to interact with the following Reactor components:

- **ApplicationClient:** interacting with applications
- **DatasetClient:** interacting with Datasets
- **DatasetModuleClient:** interacting with Dataset Modules
- **DatasetTypeClient:** interacting with Dataset Types
- **MetricsClient:** interacting with Metrics
- **MonitorClient:** monitoring System Services
- **ProcedureClient:** interacting with Procedures
- **ProgramClient:** interacting with Flows, Procedures, MapReduce jobs, User Services, and Workflows
- **QueryClient:** querying Datasets
- **ServiceClient:** interacting with User Services
- **StreamClient:** interacting with Streams

Sample Usage
============

ApplicationClient
-----------------

::

  // interact with the Reactor instance located at example.com, port 10000
  ReactorClientConfig clientConfig = new ReactorClientConfig("example.com", 10000);

  // construct the client used to interact with Reactor
  ApplicationClient appClient = new ApplicationClient(clientConfig);

  // fetch the list of applications
  List<ApplicationRecord> apps = appClient.list();

  // deploy an application
  File appJarFile = ...;
  appClient.deploy(appJarFile);

  // delete an application
  appClient.delete("Purchase");

  // list programs belonging to an application
  appClient.listPrograms("Purchase");

DatasetClient
-------------

::

  // interact with the Reactor instance located at example.com, port 10000
  ReactorClientConfig clientConfig = new ReactorClientConfig("example.com", 10000);

  // construct the client used to interact with Reactor
  DatasetClient datasetClient = new DatasetClient(clientConfig);

  // fetch list of datasets
  List<DatasetSpecification> datasets = datasetClient.list();

  // create a dataset
  datasetClient.create("someDataset", "someDatasetType");

  // delete a dataset
  datasetClient.delete("someDataset");
