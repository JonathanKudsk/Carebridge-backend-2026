package com.carebridge.services;

import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TotpService {

    public String generateSecret() {
        return new DefaultSecretGenerator().generate();
    }


    // Builds the standard otpauth URI so frontend can render a QR code for authenticator apps
    public String getOtpAuthUri(String secret, String email) {
        String label = URLEncoder.encode("Carebridge:" + email, StandardCharsets.UTF_8);
        String issuer = URLEncoder.encode("Carebridge", StandardCharsets.UTF_8);
        return "otpauth://totp/" + label
                + "?secret=" + secret
                + "&issuer=" + issuer
                + "&algorithm=SHA1&digits=6&period=30";
    }


    public boolean verifyCode(String secret, String code) {
        var generator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        var verifier = new DefaultCodeVerifier(generator, new SystemTimeProvider());
        return verifier.isValidCode(secret, code);
    }


    // For test use only — generates the currently valid TOTP code for a given secret
    public String generateCurrentCode(String secret) throws CodeGenerationException {
        var generator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        long currentBucket = Math.floorDiv(new SystemTimeProvider().getTime(), 30);
        return generator.generate(secret, currentBucket);
    }
}
