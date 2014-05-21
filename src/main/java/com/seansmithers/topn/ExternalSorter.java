package com.seansmithers.topn;

import org.apache.commons.io.LineIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExternalSorter {

    private static final int INT_SIZE_BYTES = 4;
    private String inputFile;
    private String outputFile;
    private long desiredFileSliceSize;
    private long inputFileTotalLines;
    private ExternalSortFileUtil fileUtil;

    public ExternalSorter(String inputFile, String outputFile, long desiredFileSliceSize) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.desiredFileSliceSize = desiredFileSliceSize;
        fileUtil = new ExternalSortFileUtil();
    }

    public void sortInputFile() {
        splitFileIntoSortedSlices(new File(inputFile), desiredFileSliceSize);
        mergeSortedFileSlicesIntoOutputFile();
    }

    public List<Integer> readTopNNumbersFromOutputFile(int numberOfElements) {
        return fileUtil.readTopNLinesFromFile(new File(outputFile), numberOfElements);
    }

    private void splitFileIntoSortedSlices(File file, long desiredFileSliceSize) {

        List<Integer> fileSliceData = new ArrayList<Integer>();
        LineIterator iterator = fileUtil.getLineIteratorForFile(file);
        long totalLines = 0;
        int tempFileCount = 0;

        while (iterator.hasNext()) {
            fileSliceData.add(Integer.valueOf(iterator.nextLine()));
            totalLines++;

            if (getEstimatedFileSize(fileSliceData) > desiredFileSliceSize) {
                sortAndWriteDataToFile(fileSliceData, tempFileCount);
                tempFileCount++;
                fileSliceData.clear();
            }
        }
        // Write remaining data to disk
        if (fileSliceData.size() > 0) {
            sortAndWriteDataToFile(fileSliceData, tempFileCount);
        }

        LineIterator.closeQuietly(iterator);
        inputFileTotalLines = totalLines;
    }

    private void mergeSortedFileSlicesIntoOutputFile() {
        BufferedReader[] readers = fileUtil.getBufferedReadersForFileSlices();
        int[] topNumbers = new int[readers.length];

        try {
            for (int i = 0; i < readers.length; i++) {
                topNumbers[i] = Integer.valueOf(readers[i].readLine());
            }

            List<Integer> finalSortedData = new ArrayList<Integer>();
            for (int i = 0; i < inputFileTotalLines; i++) {

                int index = findIndexOfMaxValue(topNumbers);
                finalSortedData.add(topNumbers[index]);
                topNumbers[index] = getNextValueFromFileSlice(readers[index]);

                if (getEstimatedFileSize(finalSortedData) > desiredFileSliceSize) {
                    fileUtil.writeDataToFile(finalSortedData, new File(outputFile));
                    finalSortedData.clear();
                }
            }
            // Write remaining data to disk
            if (finalSortedData.size() > 0) {
                fileUtil.writeDataToFile(finalSortedData, new File(outputFile));
            }

        } catch (IOException e) {
            throw new ExternalSortException("Error during sort", e.getCause());
        }
    }

    private long getEstimatedFileSize(List<Integer> data) {
        return (data.size() * INT_SIZE_BYTES);
    }

    private void sortAndWriteDataToFile(List<Integer> data, int tempFileSuffix) {
        Collections.sort(data, Collections.reverseOrder());
        File slice = new File(fileUtil.getFileSliceName(tempFileSuffix));
        slice.deleteOnExit();
        fileUtil.writeDataToFile(data, slice);
    }

    private int findIndexOfMaxValue(int[] values) {
        int max = Integer.MIN_VALUE;
        int index = 0;

        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                index = i;
            }
        }
        return index;
    }

    private int getNextValueFromFileSlice(BufferedReader reader) throws IOException {
        String next = reader.readLine();
        return (next == null) ? Integer.MIN_VALUE : Integer.valueOf(next);
    }
}