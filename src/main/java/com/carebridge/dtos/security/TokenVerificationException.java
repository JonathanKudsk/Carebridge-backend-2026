package com.carebridge.dtos.security;

public class TokenVerificationException extends RuntimeException {
    public TokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
