package services;

import com.carebridge.services.TotpService;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TotpServiceTest {

    private TotpService totpService;

    @BeforeEach
    void setUp() {
        totpService = new TotpService();
    }


    @Test
    void generateSecret_returnsNonEmptyBase32Secret() {
        // Arrange + Act
        String secret = totpService.generateSecret();

        // Assert
        assertNotNull(secret);
        assertFalse(secret.isEmpty());
    }


    @Test
    void getOtpAuthUri_returnsValidOtpauthUri() {
        // Arrange
        String secret = totpService.generateSecret();
        String email = "test@carebridge.io";

        // Act
        String uri = totpService.getOtpAuthUri(secret, email);

        // Assert
        assertTrue(uri.startsWith("otpauth://totp/"));
        assertTrue(uri.contains(secret));
        assertTrue(uri.contains("issuer=Carebridge"));
        assertTrue(uri.contains("digits=6"));
        assertTrue(uri.contains("period=30"));
    }


    @Test
    void verifyCode_withCurrentlyValidCode_returnsTrue() throws CodeGenerationException {
        // Arrange
        String secret = totpService.generateSecret();
        String validCode = totpService.generateCurrentCode(secret);

        // Act
        boolean result = totpService.verifyCode(secret, validCode);

        // Assert
        assertTrue(result);
    }


    @Test
    void verifyCode_withCodeFromDifferentSecret_returnsFalse() throws CodeGenerationException {
        // Arrange — generate a code for a different secret to guarantee it's wrong
        String secret = totpService.generateSecret();
        String otherSecret = totpService.generateSecret();
        String wrongCode = totpService.generateCurrentCode(otherSecret);

        // Act
        boolean result = totpService.verifyCode(secret, wrongCode);

        // Assert
        assertFalse(result);
    }


    @Test
    void generateCurrentCode_returnsSixDigitNumericCode() throws CodeGenerationException {
        // Arrange
        String secret = totpService.generateSecret();

        // Act
        String code = totpService.generateCurrentCode(secret);

        // Assert
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }
}
