package com.seansmithers.topn;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandLineOptionsTest {

    @Mock
    private CommandLineParser parser;
    @Mock
    private HelpFormatter helpFormatter;
    @Mock
    private CommandLine commandLine;
    @InjectMocks
    private CommandLineOptions commandLineOptions = new CommandLineOptions();

    @Test
    public void testParseCommandLineArguments() throws Exception {
        when(parser.parse(any(Options.class), any(String[].class))).thenReturn(commandLine);

        commandLineOptions.parseCommandLineArguments(new String[10]);
        verifyParserMethodCalls();
    }

    @Test(expected = ExternalSortException.class)
    public void testParseCommandLineArgumentsCommandLineParserThrowsException() throws Exception {
        when(parser.parse(any(Options.class), any(String[].class))).thenThrow(new ParseException("Test"));

        commandLineOptions.parseCommandLineArguments(new String[10]);
        verifyParserMethodCalls();
        verify(helpFormatter).printHelp(anyString(), any(Options.class));
    }

    private void verifyParserMethodCalls() throws ParseException {
        verify(parser).parse(any(Options.class), any(String[].class));
    }
}