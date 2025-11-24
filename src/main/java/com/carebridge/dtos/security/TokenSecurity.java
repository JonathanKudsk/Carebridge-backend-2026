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
        String rolesCsv = String.valueOf(jwt.getJWTClaimsSet().getClaim("roles"));
        String username = String.valueOf(jwt.getJWTClaimsSet().getClaim("username"));
        Set<String> roles = Arrays.stream(rolesCsv.split(",")).collect(Collectors.toSet());
        return JwtUserDTO.builder().username(username).roles(roles).build();
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
}
