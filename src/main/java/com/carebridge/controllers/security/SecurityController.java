package com.carebridge.controllers.security;

import com.carebridge.dao.security.ISecurityDAO;
import com.carebridge.security.ITokenSecurity;
import com.carebridge.security.TokenSecurity;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private final ITokenSecurity tokenSecurity = new TokenSecurity();
    private final ISecurityDAO securityDAO;

    @Value("${carebridge.jwt.issuer}")
    private String issuer;

    @Value("${carebridge.jwt.secret}")
    private String secret;

    @Value("${carebridge.jwt.expire}")
    private String expireMillis;

    public SecurityController(ISecurityDAO securityDAO) {
        this.securityDAO = securityDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            User user = securityDAO.getVerifiedUser(email, password);
            String token = createTokenInternal(Map.of("username", user.getEmail(), "roles", Set.of(user.getRole().name())));
            return ResponseEntity.ok(Map.of("token", token, "email", email));
        } catch (ValidationException e) {
            throw new ApiRuntimeException(401, e.getMessage());
        } catch (Exception e) {
            logger.error("login failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", "Internal error"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, Object> body) {
        try {
            User user = securityDAO.createUser(
                    (String) body.get("name"),
                    (String) body.get("email"),
                    (String) body.get("password"),
                    (String) body.get("displayName"),
                    (String) body.get("displayEmail"),
                    (String) body.get("displayPhone"),
                    (String) body.get("internalEmail"),
                    (String) body.get("internalPhone"),
                    Role.valueOf(((String) body.getOrDefault("role", "USER")).toUpperCase())
            );

            String token = createTokenInternal(Map.of("username", user.getEmail(), "roles", Set.of(user.getRole().name())));
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", token, "email", user.getEmail()));
        } catch (Exception e) {
            logger.error("Registration failed", e);
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    private String createTokenInternal(Map<String, Object> user) {
        String username = (String) user.get("username");
        Set<String> roles = (Set<String>) user.get("roles");
        String rolesCsv = String.join(",", roles);
        return tokenSecurity.createToken(
                username,
                rolesCsv,
                issuer,
                expireMillis,
                secret
        );
    }

    @GetMapping("/healthcheck")
    public Map<String, String> healthCheck() {
        return Map.of("msg", "API is up and running");
    }
}
