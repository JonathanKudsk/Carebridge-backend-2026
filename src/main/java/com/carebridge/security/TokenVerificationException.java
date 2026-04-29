package com.carebridge.security;

public class TokenVerificationException extends Exception {
    public TokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
