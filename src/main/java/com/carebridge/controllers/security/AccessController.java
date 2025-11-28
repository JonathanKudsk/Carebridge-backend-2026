package com.carebridge.controllers.security;

import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.entities.enums.Role;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

import java.util.Set;
import java.util.stream.Collectors;

public class AccessController implements IAccessController {

    private final SecurityController securityController = SecurityController.getInstance();

    @Override
    public void accessHandler(Context ctx) {
        Set<RouteRole> allowed = ctx.routeRoles();
        System.out.println("ACCESS DEBUG → " + ctx.method() + " " + ctx.path() + " roles=" + allowed);

        // 1️⃣ Ignorer OPTIONS (preflight) requests
        if ("OPTIONS".equalsIgnoreCase(String.valueOf(ctx.method()))) {
            return;
        }

        // 2️⃣ Hvis route er offentlig, tillad
        if (allowed.isEmpty() || allowed.contains(Role.ANYONE)) return;

        // 3️⃣ Hent Authorization header
        String header = ctx.header("Authorization");
        System.out.println("Authorization header: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Authorization header missing/malformed");
        }

        String token = header.substring("Bearer ".length());

        // 4️⃣ Verificer JWT token
        JwtUserDTO user;
        try {
            user = securityController.verifyToken(token);
            System.out.println("Verified roles: " + user.getRoles());
            ctx.attribute("user", user);
        } catch (Exception e) {
            throw new UnauthorizedResponse("You need to log in, dude! Or your token is invalid.");
        }

        // 5️⃣ Tjek at bruger har tilladte roller
        Set<String> allowedNames = allowed.stream()
                .map(Object::toString)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        boolean ok = user.getRoles().stream()
                .map(String::toUpperCase)
                .anyMatch(allowedNames::contains);

        if (!ok) {
            throw new UnauthorizedResponse("Forbidden. Needed roles: " + allowedNames);
        }
    }
}
