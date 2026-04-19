package com.carebridge.dtos.security;

import com.carebridge.dtos.JwtUserDTO;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class TokenSecurity implements ITokenSecurity {

    @Override
    public JwtUserDTO getUserWithRolesFromToken(String token) throws ParseException {
        var jwt = SignedJWT.parse(token);
        var claims = jwt.getJWTClaimsSet();

        // Temp tokens must never be used for normal authenticated access
        // So thats why this if-statement catches if we use temp tokens
        if (claims.getStringClaim("preAuthType") != null) {
            throw new TokenVerificationException("Temp tokens cannot be used for authentication", null);
        }

        String username = claims.getStringClaim("username");
        String rolesCsv = claims.getStringClaim("roles");

        if (rolesCsv == null || rolesCsv.isEmpty()) {
            throw new ParseException("No roles found in token", 0);
        }

        Set<String> roles = Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        return JwtUserDTO.builder()
                .username(username)
                .roles(roles)
                .build();
    }


    @Override
    public boolean tokenIsValid(String token, String secret) throws ParseException, TokenVerificationException {
        try {
            return SignedJWT.parse(token).verify(new MACVerifier(secret));
        } catch (JOSEException e) {
            throw new TokenVerificationException("Could not verify token", e.getCause());
        }
    }


    @Override
    public boolean tokenNotExpired(String token) throws ParseException {
        return timeToExpire(token) > 0;
    }


    @Override
    public int timeToExpire(String token) throws ParseException {
        var jwt = SignedJWT.parse(token);
        return (int) (jwt.getJWTClaimsSet().getExpirationTime().getTime() - new Date().getTime());
    }


    @Override
    public String createToken(JwtUserDTO user, String issuer, String expireMillis, String secret) {
        try {
            String rolesCsv = String.join(",", user.getRoles());
            var claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(issuer)
                    .claim("username", user.getUsername())
                    .claim("roles", rolesCsv)
                    .expirationTime(new Date(new Date().getTime() + Long.parseLong(expireMillis)))
                    .build();

            var jws = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(claims.toJSONObject()));
            jws.sign(new MACSigner(secret));
            return jws.serialize();
        } catch (Exception e) {
            throw new TokenCreationException("Could not create token", e);
        }
    }


    @Override
    public String createTempToken(String email, String preAuthType, String issuer, String secret) {
        try {
            // Temp tokens expire in 5 minutes — just enough time to complete the 2FA step
            long fiveMinutesMillis = 5 * 60 * 1000L;
            var claims = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issuer(issuer)
                    .claim("username", email)
                    .claim("preAuthType", preAuthType)
                    .expirationTime(new Date(new Date().getTime() + fiveMinutesMillis))
                    .build();

            var jws = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(claims.toJSONObject()));
            jws.sign(new MACSigner(secret));
            return jws.serialize();
        } catch (Exception e) {
            throw new TokenCreationException("Could not create temp token", e);
        }
    }


    @Override
    public String validateTempToken(String token, String expectedPreAuthType, String secret) throws ParseException {
        if (!tokenIsValid(token, secret)) {
            throw new TokenVerificationException("Temp token signature is invalid", null);
        }
        if (!tokenNotExpired(token)) {
            throw new TokenVerificationException("Temp token has expired", null);
        }

        var claims = SignedJWT.parse(token).getJWTClaimsSet();
        String preAuthType = claims.getStringClaim("preAuthType");

        if (preAuthType == null || !preAuthType.equals(expectedPreAuthType)) {
            throw new TokenVerificationException(
                    "Temp token scope mismatch: expected " + expectedPreAuthType + " but got " + preAuthType, null);
        }

        return claims.getStringClaim("username");
    }
}
