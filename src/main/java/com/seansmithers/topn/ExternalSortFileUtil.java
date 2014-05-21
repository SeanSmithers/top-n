package com.seansmithers.topn;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExternalSortFileUtil {

    private final static Logger LOGGER = Logger.getLogger(ExternalSortFileUtil.class.getName());
    private static final String CHAR_ENCODING = Charset.defaultCharset().displayName();
    public static final String TEMP_DIR = FileUtils.getTempDirectoryPath() + "external-sort/";
    public static final String TEMP_SLICE_FILE_NAME = "slice-{0}";

    public ExternalSortFileUtil() {
        try {
            FileUtils.forceMkdir(new File(TEMP_DIR));
        } catch (IOException e) {
            throw new ExternalSortException("Error creating temp directory", e.getCause());
        }
    }

    public void writeDataToFile(List<Integer> data, File file) {
        try {
            LOGGER.log(Level.INFO, "Writing data to file: " + file.getAbsolutePath());
            FileUtils.writeLines(file, CHAR_ENCODING, data, true);
        } catch (IOException e) {
            throw new ExternalSortException(
                    "Error writing data to file: " + file.getAbsolutePath(), e.getCause());
        }
    }

    public List<Integer> readTopNLinesFromFile(File file, int numberOfLines) {

        List<Integer> numbers = new ArrayList<Integer>(numberOfLines);
        LineIterator iterator = getLineIteratorForFile(file);

        while (iterator.hasNext() && (numbers.size() < numberOfLines)) {
            numbers.add(Integer.valueOf(iterator.nextLine()));
        }

        LineIterator.closeQuietly(iterator);
        return numbers;
    }

    public LineIterator getLineIteratorForFile(File file) {
        try {
            return FileUtils.lineIterator(file, CHAR_ENCODING);
        } catch (IOException e) {
            throw new ExternalSortException(
                    "Error getting line iterator for file: " + file.getAbsolutePath(), e.getCause());
        }
    }

    public String getFileSliceName(int tempFileSuffix) {
        return ExternalSortFileUtil.TEMP_DIR
                + MessageFormat.format(ExternalSortFileUtil.TEMP_SLICE_FILE_NAME, tempFileSuffix);
    }

    public List<File> getFileSlices() {
        return Lists.newArrayList(FileUtils.listFiles(new File(TEMP_DIR), null, false));
    }

    public BufferedReader[] getBufferedReadersForFileSlices() {
        List<File> fileSlices = getFileSlices();
        BufferedReader[] readers = new BufferedReader[fileSlices.size()];
        try {
            for (int i = 0; i < readers.length; i++) {
                readers[i] = new BufferedReader(new FileReader(fileSlices.get(i)));
            }
        } catch(IOException e) {
            throw new ExternalSortException(
                    "Error getting readers for file slices", e.getCause());
        }
        return readers;
    }
}