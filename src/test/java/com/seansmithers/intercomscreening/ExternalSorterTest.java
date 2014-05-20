package com.seansmithers.intercomscreening;

import com.google.common.collect.Lists;
import org.apache.commons.io.LineIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExternalSorterTest {

    private static final Random RANDOM = new Random();
    private static final String INPUT_FILE = "input.txt";
    private static final String OUTPUT_FILE = "output.txt";
    private static final long FILE_SLICE_SIZE = 1000;
    private static final int FILE_LINE_COUNT = 5;
    private int lineCount;

    @Mock
    private ExternalSortFileUtil fileUtil;
    @Mock
    private LineIterator lineIterator;
    @Mock
    private BufferedReader bufferedReader;
    @InjectMocks
    private ExternalSorter externalSorter = new ExternalSorter(INPUT_FILE, OUTPUT_FILE, FILE_SLICE_SIZE);

    @Before
    public void setUp() throws IOException {
        lineCount = 0;
        setUpFileUtilsMocks();
        setUpLineIteratorMocks();
        setUpBufferedReaderMocks();
    }

    @Test
    public void testSortInputFile() throws IOException {
        externalSorter.sortInputFile();
        verifyFileUtilMocks();
        verifyLineIteratorMocks();
        verifyBufferedReaderMocks();
    }

    @Test(expected = ExternalSortException.class)
    public void testSortInputFileFileUtilWriteDataToFileThrowsException() throws IOException {
        doThrow(new ExternalSortException()).when(fileUtil).writeDataToFile(anyListOf(Integer.class), any(File.class));
        externalSorter.sortInputFile();
        verify(lineIterator);
    }

    private void setUpFileUtilsMocks() {
        when(fileUtil.getLineIteratorForFile(any(File.class))).thenReturn(lineIterator);
        when(fileUtil.getFileSliceName(anyInt())).thenCallRealMethod();
        when(fileUtil.getBufferedReadersForFileSlices()).thenReturn(new BufferedReader[]{bufferedReader});
        doNothing().when(fileUtil).writeDataToFile(anyListOf(Integer.class), any(File.class));
    }

    private void verifyFileUtilMocks() {
        verify(fileUtil).getLineIteratorForFile(any(File.class));
        verify(fileUtil).getFileSliceName(anyInt());
        verify(fileUtil, atLeastOnce()).writeDataToFile(anyListOf(Integer.class), any(File.class));
        verify(fileUtil).getBufferedReadersForFileSlices();
    }

    private void setUpLineIteratorMocks() {
        when(lineIterator.hasNext()).thenAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                lineCount++;
                return (lineCount == FILE_LINE_COUNT) ? false : true;
            }
        });
        when(lineIterator.nextLine()).thenReturn(String.valueOf(RANDOM.nextInt()));
    }

    private void verifyLineIteratorMocks() {
        verify(lineIterator, atLeastOnce()).hasNext();
        verify(lineIterator, atLeastOnce()).nextLine();
    }

    private void setUpBufferedReaderMocks() throws IOException {
        when(bufferedReader.readLine()).thenReturn(String.valueOf(RANDOM.nextInt()));
    }

    private void verifyBufferedReaderMocks() throws IOException {
        verify(bufferedReader, atLeastOnce()).readLine();
    }

    private Collection<File> getMockedFileSlices() {
        return Lists.newArrayList(new File("slice-0"));
    }
}