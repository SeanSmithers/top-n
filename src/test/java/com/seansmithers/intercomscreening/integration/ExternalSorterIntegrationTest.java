package com.seansmithers.intercomscreening.integration;

import com.seansmithers.intercomscreening.ExternalSorter;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ExternalSorterIntegrationTest {

    private static final String INPUT_FILE =
            ExternalSorterIntegrationTest.class.getResource("/input.txt").getFile();
    private static final String OUTPUT_FILE = FileUtils.getTempDirectoryPath() + "/output.txt";
    private static final long SLICE_SIZE = FileUtils.ONE_KB;
    private static final int NUMBER_OF_ELEMENTS = 3;

    @After
    public void cleanup() {
        try {
            FileUtils.forceDelete(new File(OUTPUT_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExternalSorter() {
        ExternalSorter externalSorter = new ExternalSorter(INPUT_FILE, OUTPUT_FILE, SLICE_SIZE);
        externalSorter.sortInputFile();
        List<Integer> result = externalSorter.readTopNNumbersFromOutputFile(NUMBER_OF_ELEMENTS);

        assertThat(result.size(), is(NUMBER_OF_ELEMENTS));
        assertThat(result, is(getExpectedResult()));
    }

    private List<Integer> getExpectedResult() {
        List<Integer> result = new ArrayList<Integer>(10);
        result.add(100);
        result.add(99);
        result.add(98);
        return result;
    }
}