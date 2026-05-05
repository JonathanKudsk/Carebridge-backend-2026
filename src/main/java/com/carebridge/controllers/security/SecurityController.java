package com.carebridge.controllers.security;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.security.ISecurityDAO;
import com.carebridge.dao.security.SecurityDAO;
import com.carebridge.dtos.AuthRequest;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.RegisterUserDTO;
import com.carebridge.dtos.TotpCodeRequest;
import com.carebridge.dtos.UserDTO;
import com.carebridge.dtos.security.ITokenSecurity;
import com.carebridge.dtos.security.TokenSecurity;
import com.carebridge.dtos.security.TokenVerificationException;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.NotAuthorizedException;
import com.carebridge.exceptions.ValidationException;
import com.carebridge.services.TotpService;
import com.carebridge.services.mappers.UserMapper;
import com.carebridge.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.carebridge.dtos.ChangeRoleRequestDTO;

import java.text.ParseException;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private static ISecurityDAO securityDAO;
    private static SecurityController instance;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ITokenSecurity tokenSecurity = new TokenSecurity();
    private final TotpService totpService = new TotpService();

    private SecurityController() {
    }

    public static synchronized SecurityController getInstance() {
        if (instance == null) instance = new SecurityController();
        securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        return instance;
    }

    @Override
    public Handler login() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                AuthRequest req = ctx.bodyAsClass(AuthRequest.class);
                User verified = securityDAO.getVerifiedUser(req.getEmail(), req.getPassword());

                if (!verified.isTotpEnabled()) {
                    // First login or abandoned setup — must complete TOTP setup before getting a full JWT
                    String tempToken = createTempToken(verified.getEmail(), "SETUP");
                    ctx.status(200).json(out.put("requiresTotpSetup", true).put("tempToken", tempToken));
                } else if (isWithinGracePeriod(verified)) {
                    // Freshly enrolled — grace period active, skip 2FA challenge
                    String token = createToken(buildJwtUser(verified));
                    ctx.status(200).json(out.put("token", token)
                            .put("email", verified.getEmail())
                            .put("role", verified.getRole().name()));
                } else {
                    // Returning user with 2FA active — must verify TOTP code
                    String tempToken = createTempToken(verified.getEmail(), "VERIFY");
                    ctx.status(200).json(out.put("requires2FA", true).put("tempToken", tempToken));
                }
            } catch (ValidationException e) {
                ctx.status(401).json(out.put("msg", e.getMessage()));
            } catch (Exception e) {
                logger.error("login failed", e);
                ctx.status(500).json(out.put("msg", "Internal error"));
            }
        };
    }

    @Override
    public Handler totpSetup() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                String email = validateTempToken(ctx, "SETUP");
                String secret = totpService.generateSecret();
                securityDAO.saveTotpSecret(email, secret);
                String otpauthUri = totpService.getOtpAuthUri(secret, email);
                ctx.status(200).json(out.put("secret", secret).put("otpauthUri", otpauthUri));
            } catch (TokenVerificationException e) {
                ctx.status(401).json(out.put("msg", e.getMessage()));
            } catch (Exception e) {
                logger.error("totpSetup failed", e);
                ctx.status(500).json(out.put("msg", "Internal error"));
            }
        };
    }

    @Override
    public Handler totpConfirm() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                String email = validateTempToken(ctx, "SETUP");
                TotpCodeRequest req = ctx.bodyAsClass(TotpCodeRequest.class);
                User user = securityDAO.getUserByEmail(email);

                if (!totpService.verifyCode(user.getTotpSecret(), req.getCode())) {
                    ctx.status(401).json(out.put("msg", "Invalid TOTP code"));
                    return;
                }

                securityDAO.enableTotp(email);
                securityDAO.renewGracePeriod(email);

                String token = createToken(buildJwtUser(user));
                ctx.status(200).json(out.put("token", token)
                        .put("email", email)
                        .put("role", user.getRole().name()));
            } catch (TokenVerificationException e) {
                ctx.status(401).json(out.put("msg", e.getMessage()));
            } catch (Exception e) {
                logger.error("totpConfirm failed", e);
                ctx.status(500).json(out.put("msg", "Internal error"));
            }
        };
    }

    @Override
    public Handler totpVerify() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                String email = validateTempToken(ctx, "VERIFY");
                TotpCodeRequest req = ctx.bodyAsClass(TotpCodeRequest.class);
                User user = securityDAO.getUserByEmail(email);

                if (!totpService.verifyCode(user.getTotpSecret(), req.getCode())) {
                    ctx.status(401).json(out.put("msg", "Invalid TOTP code"));
                    return;
                }

                securityDAO.renewGracePeriod(email);

                String token = createToken(buildJwtUser(user));
                ctx.status(200).json(out.put("token", token)
                        .put("email", email)
                        .put("role", user.getRole().name()));
            } catch (TokenVerificationException e) {
                ctx.status(401).json(out.put("msg", e.getMessage()));
            } catch (Exception e) {
                logger.error("totpVerify failed", e);
                ctx.status(500).json(out.put("msg", "Internal error"));
            }
        };
    }

    @Override
    public Handler register() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                // Parse hele kroppen som RegisterUserDTO
                RegisterUserDTO dto = ctx.bodyAsClass(RegisterUserDTO.class);

                // Opret bruger i DB via SecurityDAO med alle felter
                User created = securityDAO.createUser(
                        dto.getName(),
                        dto.getEmail(),
                        dto.getPassword(),
                        dto.getDisplayName(),
                        dto.getDisplayEmail(),
                        dto.getDisplayPhone(),
                        dto.getInternalEmail(),
                        dto.getInternalPhone(),
                        dto.getRole()
                );

                // Lav JWT
                JwtUserDTO jwtUser = JwtUserDTO.builder()
                        .username(created.getEmail())
                        .roles(Set.of(created.getRole().name()))
                        .build();

                String token = createToken(jwtUser);

                // Returnér token + bruger info
                ctx.status(HttpStatus.CREATED).json(out
                        .put("token", token)
                        .put("email", created.getEmail())
                        .put("role", created.getRole().name())
                        .put("displayName", created.getDisplayName())
                        .put("displayEmail", created.getDisplayEmail())
                        .put("displayPhone", created.getDisplayPhone())
                        .put("internalEmail", created.getInternalEmail())
                        .put("internalPhone", created.getInternalPhone())
                );
            } catch (ApiRuntimeException e) {
                ctx.status(e.getErrorCode()).json(out.put("msg", e.getMessage()));
            } catch (Exception e) {
                logger.error("register failed", e);
                ctx.status(500).json(out.put("msg", "Internal error"));
            }
        };
    }




    @Override
    public Handler authenticate() {
        return ctx -> {
            if ("OPTIONS".equalsIgnoreCase(ctx.method().toString())) {
                ctx.status(200);
                return;
            }

            String header = ctx.header("Authorization");
            if (header == null || !header.startsWith("Bearer "))
                throw new UnauthorizedResponse("Authorization header missing/malformed");

            String token = header.substring("Bearer ".length());
            JwtUserDTO verifiedTokenUser = verifyToken(token);
            if (verifiedTokenUser == null) throw new UnauthorizedResponse("Invalid User or Token");

            logger.info("User verified: {}", verifiedTokenUser.getUsername());
            ctx.attribute("user", verifiedTokenUser);
        };
    }

    @Override
    public boolean authorize(JwtUserDTO user, Set<RouteRole> allowedRoles) {
        if (user == null) throw new UnauthorizedResponse("You need to log in, dude!");
        var allowed = allowedRoles.stream().map(RouteRole::toString).collect(Collectors.toSet());
        return user.getRoles().stream().map(String::toUpperCase).anyMatch(allowed::contains);
    }

    @Override
    public String createToken(JwtUserDTO user) {
        try {
            final boolean DEPLOYED = System.getenv("DEPLOYED") != null;
            String ISSUER = DEPLOYED ? System.getenv("ISSUER") : Utils.getPropertyValue("ISSUER", "application.properties");
            String EXPIRE = DEPLOYED ? System.getenv("TOKEN_EXPIRE_TIME") : Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "application.properties");
            String SECRET = DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "application.properties");

            logger.info("Creating token with ISSUER={}, EXPIRE={}, SECRET={}", ISSUER, EXPIRE, SECRET != null ? "***" : "null");

            return tokenSecurity.createToken(user, ISSUER, EXPIRE, SECRET);
        } catch (Exception e) {
            logger.error("Token creation failed", e);
            throw new ApiRuntimeException(500, "Could not create token");
        }
    }


    @Override
    public JwtUserDTO verifyToken(String token) {
        boolean DEPLOYED = System.getenv("DEPLOYED") != null;
        String SECRET = DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "application.properties");
        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new NotAuthorizedException(403, "Token is not valid");
            }
        } catch (ParseException | NotAuthorizedException | TokenVerificationException e) {
            throw new ApiRuntimeException(401, "Unauthorized. Could not verify token");
        }
    }

    private boolean isWithinGracePeriod(User user) {
        return user.getTotpGracePeriodEnd() != null
                && Instant.now().isBefore(user.getTotpGracePeriodEnd());
    }

    private JwtUserDTO buildJwtUser(User user) {
        return JwtUserDTO.builder()
                .username(user.getEmail())
                .roles(Set.of(user.getRole().name()))
                .build();
    }

    private String createTempToken(String email, String scope) {
        boolean deployed = System.getenv("DEPLOYED") != null;
        String issuer = deployed ? System.getenv("ISSUER") : Utils.getPropertyValue("ISSUER", "application.properties");
        String secret = deployed ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "application.properties");
        return tokenSecurity.createTempToken(email, scope, issuer, secret);
    }

    private String validateTempToken(Context ctx, String expectedScope) throws ParseException {
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer "))
            throw new TokenVerificationException("Authorization header missing", null);
        String token = header.substring("Bearer ".length());
        boolean deployed = System.getenv("DEPLOYED") != null;
        String secret = deployed ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "application.properties");
        return tokenSecurity.validateTempToken(token, expectedScope, secret);
    }

    public @NotNull Handler addRole() {
        return ctx -> ctx.status(501).json("{\"msg\":\"Not implemented in enum-role model\"}");
    }

    public Handler changeRole() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();

            try {
                Long userId = Long.parseLong(ctx.pathParam("id"));
                ChangeRoleRequestDTO dto = ctx.bodyAsClass(ChangeRoleRequestDTO.class);

                if (dto == null || dto.getRole() == null) {
                    throw new ApiRuntimeException(400, "Role is required");
                }

                User updatedUser = securityDAO.changeRole(userId, dto.getRole());
                UserDTO safeUser = UserMapper.toDTO(updatedUser);

                ctx.status(200).json(out
                        .put("email", safeUser.getEmail())
                        .put("role", safeUser.getRole().name())
                );

            } catch (NumberFormatException e) {
                ctx.status(400).json(out.put("msg", "Invalid user id"));
            } catch (ApiRuntimeException e) {
                ctx.status(e.getErrorCode()).json(out.put("msg", e.getMessage()));
            } catch (Exception e) {
                logger.error("changeRole failed", e);
                ctx.status(500).json(out.put("msg", "Internal error"));
            }
        };
    }

    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}
