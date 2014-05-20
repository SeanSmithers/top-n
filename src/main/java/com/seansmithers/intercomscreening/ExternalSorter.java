package com.seansmithers.intercomscreening;

import org.apache.commons.io.LineIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sean Smithers
 */
public class ExternalSorter {

    private final static Logger LOGGER = Logger.getLogger(ExternalSorter.class.getName());
    private String inputFile;
    private String outputFile;
    private long fileSliceSize;
    private ExternalSortFileUtil fileUtil;

    public ExternalSorter(String inputFile, String outputFile, long fileSliceSize) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.fileSliceSize = fileSliceSize;
        fileUtil = new ExternalSortFileUtil();
    }

    public void sortInputFile() {
        long totalLines = splitFileIntoSortedSlices(new File(inputFile), fileSliceSize);
        mergeSortedFileSlicesIntoOutputFile(totalLines);
    }

    private long splitFileIntoSortedSlices(File file, long fileSliceSize) {

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
        return totalLines;
    }

    private void mergeSortedFileSlicesIntoOutputFile(long totalLines) {
        BufferedReader[] readers = fileUtil.getBufferedReadersForFileSlices();
        int[] topNumbers = new int[readers.length];

        try {
            for (int i = 0; i < readers.length; i++) {
                topNumbers[i] = Integer.valueOf(readers[i].readLine());
            }

            List<Integer> finalSortedNumbers = new ArrayList<Integer>();
            for (int i = 0; i < totalLines; i++) {

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
            LOGGER.log(Level.SEVERE, "Error during sort: " + e.getMessage());
            throw new ExternalSortException(e.getMessage(), e.getCause());
        }
    }

    /**
     * @param fileSliceSize Size of file slice in bytes
     * @param numbers       Data to be written to file
     * @return True if the estimated file size of data exceeds desired file size, false otherwise
     */
    private boolean shouldWriteDataToOutputFile(long fileSliceSize, List<Integer> numbers) {
        return (numbers.size() * 4) > fileSliceSize;
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