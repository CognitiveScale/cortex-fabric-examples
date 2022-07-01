package com.c12e.cortex.examples.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * CLI application for Joining Connections in a local environment.
 */
@Command(name = "local-clients", version = "v1.0",  mixinStandardHelpOptions = true, subcommands = {
        DataSourceRW.class,
        JoinConnections.class
})
public class Main {

    Logger logger = LoggerFactory.getLogger(Main.class);

    @Option(names = { "-p", "--project" }, description = "project name")
    String project;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
