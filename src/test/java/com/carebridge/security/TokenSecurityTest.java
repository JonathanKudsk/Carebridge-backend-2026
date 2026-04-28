package com.carebridge.security;

import org.junit.jupiter.api.*;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenSecurityTest {

    private final TokenSecurity tokenSecurity = new TokenSecurity();
    private final String secret = "A_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_123456";
    private String validToken;

    @Test
    @Order(1)
    void testCreateToken() {
        validToken = tokenSecurity.createToken("testuser", "ADMIN,USER", "carebridge", "3600000", secret);
        assertNotNull(validToken);
        
        // Test creation error (wrong secret size)
        assertThrows(TokenCreationException.class, () -> 
            tokenSecurity.createToken("u", "r", "i", "3600", "short")
        );
    }

    @Test
    @Order(2)
    void testTokenValidation() throws ParseException, TokenVerificationException {
        assertTrue(tokenSecurity.tokenIsValid(validToken, secret));
        assertTrue(tokenSecurity.tokenNotExpired(validToken));
        assertTrue(tokenSecurity.timeToExpire(validToken) > 0);
        
        // Invalid secret
        assertFalse(tokenSecurity.tokenIsValid(validToken, "ANOTHER_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES"));
    }

    @Test
    @Order(3)
    void testGetRoles() throws ParseException {
        Map<String, Object> data = tokenSecurity.getUserWithRolesFromToken(validToken);
        assertEquals("testuser", data.get("username"));
        Set<String> roles = (Set<String>) data.get("roles");
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("USER"));
    }

    @Test
    @Order(4)
    void testErrors() {
        assertThrows(ParseException.class, () -> tokenSecurity.getUserWithRolesFromToken("invalid.token.here"));
        
        String noRolesToken = tokenSecurity.createToken("u", "", "i", "3600", secret);
        assertThrows(ParseException.class, () -> tokenSecurity.getUserWithRolesFromToken(noRolesToken));
        
        String expiredToken = tokenSecurity.createToken("u", "r", "i", "-1000", secret);
        try {
            assertFalse(tokenSecurity.tokenNotExpired(expiredToken));
        } catch (ParseException e) {}
    }
}
