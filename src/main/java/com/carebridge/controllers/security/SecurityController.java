package com.carebridge.controllers.security;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.security.ISecurityDAO;
import com.carebridge.dao.security.SecurityDAO;
import com.carebridge.dtos.AuthRequest;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.RegisterUserDTO;
import com.carebridge.dtos.UserDTO;
import com.carebridge.dtos.security.ITokenSecurity;
import com.carebridge.dtos.security.TokenSecurity;
import com.carebridge.dtos.security.TokenVerificationException;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.NotAuthorizedException;
import com.carebridge.exceptions.ValidationException;
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

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private static ISecurityDAO securityDAO;
    private static SecurityController instance;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ITokenSecurity tokenSecurity = new TokenSecurity();

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

                JwtUserDTO jwtUser = JwtUserDTO.builder()
                        .username(verified.getEmail())
                        .roles(Set.of(verified.getRole().name()))
                        .build();

                String token = createToken(jwtUser);
                UserDTO safeUser = UserMapper.toDTO(verified);

                long expiresAt = resolveExpiresAt();
                ctx.status(200).json(out.put("token", token)
                        .put("email", safeUser.getEmail())
                        .put("role", safeUser.getRole().name())
                        .put("expiresAt", expiresAt)
                        .put("warningAt", expiresAt - 30_000));
            } catch (ValidationException e) {
                ctx.status(401).json(out.put("msg", e.getMessage()));
            } catch (Exception e) {

                logger.error("login failed", e);
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

    public @NotNull Handler addRole() {
        return ctx -> ctx.status(501).json("{\"msg\":\"Not implemented in enum-role model\"}");
    }

    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }

    // POST /auth/refresh — udsted nyt token og invalider det gamle
    public Handler refresh() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                String header = ctx.header("Authorization");
                if (header == null || !header.startsWith("Bearer ")) {
                    ctx.status(401).json(out.put("msg", "Authorization header missing"));
                    return;
                }
                String oldToken = header.substring("Bearer ".length());

                if (TokenBlacklist.contains(oldToken)) {
                    ctx.status(401).json(out.put("msg", "Token already invalidated"));
                    return;
                }

                JwtUserDTO user = verifyToken(oldToken);
                TokenBlacklist.add(oldToken);

                String newToken = createToken(user);
                long expiresAt = resolveExpiresAt();

                ctx.status(200).json(out
                        .put("token", newToken)
                        .put("expiresAt", expiresAt)
                        .put("warningAt", expiresAt - 30_000));
            } catch (Exception e) {
                ctx.status(401).json(out.put("msg", "Could not refresh token — please log in again"));
            }
        };
    }

    // POST /auth/logout — invalider det aktuelle token
    public Handler logout() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            String header = ctx.header("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                TokenBlacklist.add(header.substring("Bearer ".length()));
            }
            ctx.status(200).json(out.put("msg", "Logged out successfully"));
        };
    }

    // GET /auth/session — returnér resterende tid på token
    public Handler sessionInfo() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                String header = ctx.header("Authorization");
                if (header == null || !header.startsWith("Bearer ")) {
                    ctx.status(401).json(out.put("msg", "Not authenticated"));
                    return;
                }
                String token = header.substring("Bearer ".length());

                if (TokenBlacklist.contains(token)) {
                    ctx.status(401).json(out.put("msg", "Token invalidated"));
                    return;
                }

                int timeToExpireMs = tokenSecurity.timeToExpire(token);
                if (timeToExpireMs <= 0) {
                    ctx.status(401).json(out.put("msg", "Token expired"));
                    return;
                }

                long expiresAt = System.currentTimeMillis() + timeToExpireMs;
                ctx.status(200).json(out
                        .put("timeToExpireMs", timeToExpireMs)
                        .put("expiresAt", expiresAt)
                        .put("warningAt", expiresAt - 30_000));
            } catch (Exception e) {
                ctx.status(401).json(out.put("msg", "Invalid token"));
            }
        };
    }

    private long resolveExpiresAt() {
        boolean deployed = System.getenv("DEPLOYED") != null;
        String expireStr = deployed
                ? System.getenv("TOKEN_EXPIRE_TIME")
                : Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "application.properties");
        return System.currentTimeMillis() + Long.parseLong(expireStr);
    }
}
