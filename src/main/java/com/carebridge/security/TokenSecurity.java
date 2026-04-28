package com.carebridge.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TokenSecurity implements ITokenSecurity {

    @Override
    public Map<String, Object> getUserWithRolesFromToken(String token) throws ParseException {
        var jwt = SignedJWT.parse(token);
        var claims = jwt.getJWTClaimsSet();

        String username = claims.getStringClaim("username");
        String rolesCsv = claims.getStringClaim("roles");

        if (rolesCsv == null || rolesCsv.isEmpty()) {
            throw new ParseException("No roles found in token", 0);
        }

        Set<String> roles = Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        return Map.of(
            "username", username,
            "roles", roles
        );
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
    public String createToken(String username, String rolesCsv, String issuer, String expireMillis, String secret) {
        try {
            var claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issuer(issuer)
                    .claim("username", username)
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
}
