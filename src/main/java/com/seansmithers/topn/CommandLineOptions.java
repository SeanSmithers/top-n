package com.seansmithers.topn;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineOptions {

    private CommandLineParser parser;
    private HelpFormatter helpFormatter;

    public CommandLineOptions() {
        this.parser = new BasicParser();
        this.helpFormatter = new HelpFormatter();
    }

    public CommandLine parseCommandLineArguments(String[] args) {
        CommandLine cmdLine;
        Options options = getOptions();
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            printUsage(options);
            throw new ExternalSortException(e.getMessage(), e.getCause());
        }
        return cmdLine;
    }

    private Options getOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.withDescription("Input file")
                .isRequired()
                .hasArg()
                .create("i"));
        options.addOption(OptionBuilder.withDescription("Output file")
                .isRequired()
                .hasArg()
                .create("o"));
        options.addOption(OptionBuilder.withDescription("File slice size in bytes. Defaults to 10MB")
                .hasArg()
                .create("b"));
        options.addOption(OptionBuilder.withDescription("Number of elements to find")
                .isRequired()
                .withType(Integer.class)
                .hasArg()
                .create("n"));
        return options;
    }

    private void printUsage(Options options) {
        helpFormatter.printHelp("java -jar top-n-1.0-jar-with-dependencies.jar [OPTIONS]", options);
    }
}