package com.seansmithers.topn;

public class ExternalSortException extends RuntimeException {

    public ExternalSortException() {
        super();
    }

    public ExternalSortException(String message, Throwable throwable) {
        super(message, throwable);
    }
}