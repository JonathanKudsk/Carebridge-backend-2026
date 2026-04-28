package com.carebridge.security;

import java.text.ParseException;
import java.util.Map;

public interface ITokenSecurity {
    Map<String, Object> getUserWithRolesFromToken(String token) throws ParseException;
    boolean tokenIsValid(String token, String secret) throws ParseException, TokenVerificationException;
    boolean tokenNotExpired(String token) throws ParseException;
    int timeToExpire(String token) throws ParseException;
    String createToken(String username, String rolesCsv, String issuer, String expireMillis, String secret) throws TokenCreationException;
}
