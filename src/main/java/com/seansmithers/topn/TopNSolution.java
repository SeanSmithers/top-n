package com.seansmithers.topn;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopNSolution {

    private final static Logger LOGGER = Logger.getLogger(TopNSolution.class.getName());
    private static String inputFile;
    private static String outputFile;
    private static long fileSliceSize = FileUtils.ONE_MB * 10;
    private static int numberOfElements;

    public static void main(String[] args) throws IOException {

        processCommandLineArguments(args);

        ExternalSorter externalSorter = new ExternalSorter(inputFile, outputFile, fileSliceSize);
        externalSorter.sortInputFile();
        LOGGER.log(Level.INFO, "Top "
                + numberOfElements
                + " numbers in "
                + inputFile
                + ": "
                + externalSorter.readTopNNumbersFromOutputFile(numberOfElements).toString());
    }

    private static void processCommandLineArguments(String[] args) {

        CommandLineOptions cmdLineOptions = new CommandLineOptions();
        CommandLine cmdLine = null;
        try {
            cmdLine = cmdLineOptions.parseCommandLineArguments(args);
        } catch (ExternalSortException e) {
            System.exit(1);
        }

        if (cmdLine.hasOption("i")) {
            inputFile = cmdLine.getOptionValue("i");
        }
        if (cmdLine.hasOption("o")) {
            outputFile = cmdLine.getOptionValue("o");
        }
        if (cmdLine.hasOption("b")) {
            fileSliceSize = Long.parseLong(cmdLine.getOptionValue("b"));
        }
        if (cmdLine.hasOption("n")) {
            numberOfElements = Integer.parseInt(cmdLine.getOptionValue("n"));
        }
    }
}