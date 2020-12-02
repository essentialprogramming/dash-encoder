package com.exception;

import com.util.exceptions.ErrorCodes;

/**
 * Error code enum for streaming API.
 */
public enum ErrorCode implements ErrorCodes.ErrorCode {


    ERROR_READING_FILE(100, "Could not read file");



    static {
        ErrorCodes.registerErrorCodes(com.exception.ErrorCode.class);
    }

    private final long code;
    private final String description;

    ErrorCode(long code, String description) {
        this.code = code;
        this.description = description;
    }

    public long getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

}
