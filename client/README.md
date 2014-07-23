# Java client library

## Introduction

The Java client library provides a way to interact with Reactor using Java code.

## Dependency

```
<dependency>
  <groupId>com.continuuity</groupId>
  <artifactId>client</artifactId>
  <version>${reactor.version}</version>
</dependency>
```

## Components

`ApplicationClient`: provides ways to interact with applications
`DatasetClient`: provides ways to interact with datasets
`DatasetModuleClient`: provides ways to interact with dataset modules
`DatasetTypeClient`: provides ways to interact with dataset types
`MetricsClient`: provides ways to interact with metrics
`MonitorClient`: provides ways to monitor Reactor system services
`ProcedureClient`: provides ways to interact with procedures
`ProgramClient`: provides ways to interact with programs (flows, procedures, mapreduce, services, and workflows)
`QueryClient`: provides ways to query datasets
`ServiceClient`: provides ways to interact with application services
`StreamClient`: provides ways to interact with streams

## Sample usage: retrieving the list of deployed applications

```
// interact with the Reactor instance located at example.com, port 10000
ReactorClientConfig clientConfig = new ReactorClientConfig("example.com", 10000);

// construct the client used to interact with Reactor
ApplicationClient appClient = new ApplicationClient(clientConfig);

// fetch the list of applications
List<ApplicationRecord> apps = appClient.list();
```
