package security;

import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.security.TokenSecurity;
import com.carebridge.dtos.security.TokenVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TokenSecurityTest {

    private TokenSecurity tokenSecurity;

    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hmac-signing";
    private static final String TEST_ISSUER = "carebridge-test";
    private static final String TEST_EMAIL = "test@carebridge.io";


    @BeforeEach
    void setUp() {
        tokenSecurity = new TokenSecurity();
    }


    @Test
    void createTempToken_withSetupScope_returnsSignedToken() {
        // Arrange + Act
        String token = tokenSecurity.createTempToken(TEST_EMAIL, "SETUP", TEST_ISSUER, TEST_SECRET);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }


    @Test
    void validateTempToken_withCorrectScope_returnsEmail() throws ParseException {
        // Arrange
        String token = tokenSecurity.createTempToken(TEST_EMAIL, "SETUP", TEST_ISSUER, TEST_SECRET);

        // Act
        String email = tokenSecurity.validateTempToken(token, "SETUP", TEST_SECRET);

        // Assert
        assertEquals(TEST_EMAIL, email);
    }


    @Test
    void validateTempToken_withWrongScope_throwsTokenVerificationException() {
        // Arrange — token scoped to SETUP must be rejected when VERIFY is expected
        String token = tokenSecurity.createTempToken(TEST_EMAIL, "SETUP", TEST_ISSUER, TEST_SECRET);

        // Act + Assert
        assertThrows(TokenVerificationException.class, () ->
                tokenSecurity.validateTempToken(token, "VERIFY", TEST_SECRET));
    }


    @Test
    void validateTempToken_withWrongSecret_throwsException() {
        // Arrange
        String token = tokenSecurity.createTempToken(TEST_EMAIL, "SETUP", TEST_ISSUER, TEST_SECRET);
        String wrongSecret = "wrong-secret-key-that-is-long-enough-for-hmac-signing";

        // Act + Assert
        assertThrows(Exception.class, () ->
                tokenSecurity.validateTempToken(token, "SETUP", wrongSecret));
    }


    @Test
    void getUserWithRolesFromToken_withTempToken_throwsTokenVerificationException() {
        // Arrange — temp tokens must never grant access to protected endpoints
        String tempToken = tokenSecurity.createTempToken(TEST_EMAIL, "SETUP", TEST_ISSUER, TEST_SECRET);

        // Act + Assert
        assertThrows(TokenVerificationException.class, () ->
                tokenSecurity.getUserWithRolesFromToken(tempToken));
    }


    @Test
    void getUserWithRolesFromToken_withFullToken_returnsUserWithRoles() throws ParseException {
        // Arrange
        JwtUserDTO user = JwtUserDTO.builder()
                .username(TEST_EMAIL)
                .roles(Set.of("USER"))
                .build();
        String fullToken = tokenSecurity.createToken(user, TEST_ISSUER, "3600000", TEST_SECRET);

        // Act
        JwtUserDTO result = tokenSecurity.getUserWithRolesFromToken(fullToken);

        // Assert
        assertEquals(TEST_EMAIL, result.getUsername());
        assertTrue(result.getRoles().contains("USER"));
    }
}
