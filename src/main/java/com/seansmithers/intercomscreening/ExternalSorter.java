package com.seansmithers.intercomscreening;

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
    private long fileSliceSize;
    private long inputFileTotalLines;
    private ExternalSortFileUtil fileUtil;

    public ExternalSorter(String inputFile, String outputFile, long fileSliceSize) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.fileSliceSize = fileSliceSize;
        fileUtil = new ExternalSortFileUtil();
    }

    public void sortInputFile() {
        splitFileIntoSortedSlices(new File(inputFile), fileSliceSize);
        mergeSortedFileSlicesIntoOutputFile();
    }

    private void splitFileIntoSortedSlices(File file, long fileSliceSize) {

        List<Integer> numbers = new ArrayList<Integer>();
        LineIterator iterator = fileUtil.getLineIteratorForFile(file);
        long totalLines = 0;
        int tempFileCount = 0;

        while (iterator.hasNext()) {
            numbers.add(Integer.valueOf(iterator.nextLine()));
            totalLines++;

            if (shouldWriteDataToOutputFile(fileSliceSize, numbers)) {
                sortAndWriteDataToFile(numbers, tempFileCount);
                tempFileCount++;
                numbers.clear();
            }
        }
        // Write remaining data to disk
        if (numbers.size() > 0) {
            sortAndWriteDataToFile(numbers, tempFileCount);
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

            List<Integer> finalSortedNumbers = new ArrayList<Integer>();
            for (int i = 0; i < inputFileTotalLines; i++) {

                int index = findIndexOfMaxValue(topNumbers);
                finalSortedNumbers.add(topNumbers[index]);
                topNumbers[index] = getNextValueFromFileSlice(readers[index]);

                if (shouldWriteDataToOutputFile(fileSliceSize, finalSortedNumbers)) {
                    fileUtil.writeDataToFile(finalSortedNumbers, new File(outputFile));
                    finalSortedNumbers.clear();
                }
            }
            // Write remaining data to disk
            if (finalSortedNumbers.size() > 0) {
                fileUtil.writeDataToFile(finalSortedNumbers, new File(outputFile));
            }

        } catch (IOException e) {
            throw new ExternalSortException("Error during sort", e.getCause());
        }
    }

    /**
     * @param fileSliceSize Size of file slice in bytes
     * @param data          Data to be written to file
     * @return True if the estimated file size of data exceeds desired file size, false otherwise
     */
    private boolean shouldWriteDataToOutputFile(long fileSliceSize, List<Integer> data) {
        return (data.size() * INT_SIZE_BYTES) > fileSliceSize;
    }

    private void sortAndWriteDataToFile(List<Integer> numbers, int tempFileSuffix) {
        Collections.sort(numbers, Collections.reverseOrder());
        File slice = new File(fileUtil.getFileSliceName(tempFileSuffix));
        slice.deleteOnExit();
        fileUtil.writeDataToFile(numbers, slice);
    }

    private int findIndexOfMaxValue(int[] topNumbers) {
        int max = Integer.MIN_VALUE;
        int index = 0;

        for (int i = 0; i < topNumbers.length; i++) {
            if (topNumbers[i] > max) {
                index = i;
            }
        }
        return index;
    }

    private int getNextValueFromFileSlice(BufferedReader reader) throws IOException {
        String next = reader.readLine();
        return (next == null) ? Integer.MIN_VALUE : Integer.valueOf(next);
    }

    public List<Integer> readTopNNumbersFromOutputFile(int numberOfElements) {
        return fileUtil.readTopNLinesFromFile(new File(outputFile), numberOfElements);
    }
}