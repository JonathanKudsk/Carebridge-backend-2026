package com.carebridge.exceptions;

public class ConflictException extends RuntimeException {

    private final Long currentVersion;

    public ConflictException(String message, Long currentVersion) {
        super(message);
        this.currentVersion = currentVersion;
    }

    public Long getCurrentVersion() {
        return currentVersion;
    }
}
