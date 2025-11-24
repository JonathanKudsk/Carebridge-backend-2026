package com.carebridge.dtos.security;

import com.carebridge.dtos.JwtUserDTO;

import java.text.ParseException;

public interface ITokenSecurity {
    JwtUserDTO getUserWithRolesFromToken(String token) throws ParseException;

    boolean tokenIsValid(String token, String secret) throws ParseException, TokenVerificationException;

    boolean tokenNotExpired(String token) throws ParseException;

    int timeToExpire(String token) throws ParseException;

    String createToken(JwtUserDTO user, String issuer, String expireMillis, String secret) throws TokenCreationException;
}

