package com.seansmithers.topn;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class ExternalSortFileUtilTest {

    private static final Random RANDOM = new Random();
    @Mock
    private LineIterator lineIterator;
    private ExternalSortFileUtil fileUtil;

    @Before
    public void setUp() throws Exception {
        setUpFileUtilsMocks();
        fileUtil = new ExternalSortFileUtil();
    }

    private void setUpFileUtilsMocks() throws IOException {
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.when(FileUtils.lineIterator(any(File.class), anyString())).thenReturn(lineIterator);
        PowerMockito.when(FileUtils.listFiles(any(File.class), any(String[].class), anyBoolean())).thenReturn(getMockedFileSlices());
    }

    @Test
    public void testWriteDataToFile() {
        fileUtil.writeDataToFile(Lists.newArrayList(1, 2, 3), new File("test"));
        PowerMockito.verifyStatic();
    }

    @Test(expected = ExternalSortException.class)
    public void testWriteDataToFileThrowsException() throws Exception {
        PowerMockito.doThrow(new IOException()).when(FileUtils.class);
        FileUtils.writeLines(any(File.class), anyString(), anyListOf(Integer.class), anyBoolean());

        fileUtil.writeDataToFile(Lists.newArrayList(1, 2, 3), new File("test"));
        PowerMockito.verifyStatic();
    }

    @Test
    public void testReadTopNLinesFromFile() {
        when(lineIterator.hasNext()).thenReturn(true);
        when(lineIterator.nextLine()).thenReturn(String.valueOf(RANDOM.nextInt()));

        List<Integer> result = fileUtil.readTopNLinesFromFile(new File(""), 3);
        assertThat(result.size(), is(3));
        PowerMockito.verifyStatic();
    }

    @Test
    public void testGetLineIteratorForFile() {
        LineIterator lineIterator = fileUtil.getLineIteratorForFile(new File(""));
        assertThat(lineIterator, notNullValue());
        PowerMockito.verifyStatic();
    }

    @Test(expected = ExternalSortException.class)
    public void testGetLineIteratorForFileThrowsException() throws Exception {
        PowerMockito.when(FileUtils.lineIterator(any(File.class), anyString())).thenThrow(new IOException());

        LineIterator lineIterator = fileUtil.getLineIteratorForFile(new File(""));
        assertThat(lineIterator, notNullValue());
        PowerMockito.verifyStatic();
    }

    @Test
    public void testGetFileSliceName() {
        assertThat(fileUtil.getFileSliceName(99), is(ExternalSortFileUtil.TEMP_DIR + "slice-99"));
    }

    @Test
    public void getFileSlices() {
        List<File> result = fileUtil.getFileSlices();
        assertThat(result.size(), is(3));
        PowerMockito.verifyStatic();
    }

    private Collection<File> getMockedFileSlices() {
        Collection<File> files = new ArrayList<File>(3);
        for (int i = 0; i < 3; i++) {
            files.add(new File("slice-" + i));
        }
        return files;
    }
}