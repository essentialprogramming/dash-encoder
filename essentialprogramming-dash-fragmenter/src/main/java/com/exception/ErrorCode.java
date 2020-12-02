package com.exception;

import com.util.exceptions.ErrorCodes;

/**
 * Error code enum for Dash API.
 */
public enum ErrorCode implements ErrorCodes.ErrorCode {


    ERROR_CREATING_MPD_FILE(100, "Could not create mpd file");



    static {
        ErrorCodes.registerErrorCodes(ErrorCode.class);
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
