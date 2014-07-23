# Shell client

## Introduction

The shell client provides a way to interact with Reactor using a shell, similar to hbase shell or bash.

## How to build

```
mvn clean package -DskipTests -pl reactor-shell -am
```

The shell executable is located at `reactor-shell/target/reactor`.

## Usage

The shell may be used in two ways: 1) interactive mode and 2) executable mode

### Interactive mode

To run the shell in interactive mode, run the `reactor` executable with no arguments from the terminal. The executable should bring you into a shell, with the following prompt:

```
reactor (localhost)>
```

This indicates that the shell client is currently set to interact with the Reactor instance at localhost. To interact with a different Reactor instance, you may modify the environment variable `REACTOR_HOST`. For example, with `REACTOR_HOST` set to `example.com`, the shell client would be interacting with a Reactor instance at `example.com` port `10000`.

To list all of the available commands, you may enter `help`.

### Executable mode

To run the shell in executable mode, run the `reactor` executable and provide the command you wish to execute as the argument. For example, to list all applications currently deployed to Reactor, you may run `reactor list apps`.
