package com.carebridge.routes;

import com.carebridge.controllers.security.SecurityController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SecurityRoutes {
    private static final SecurityController security = SecurityController.getInstance();

    public static EndpointGroup getSecurityRoutes() {
        return () -> {
            path("/auth", () -> {
                get("/healthcheck", security::healthCheck, Role.ANYONE);
                post("/login", security.login(), Role.ANYONE);
                post("/register", security.register(), Role.ADMIN);
                post("/user/addrole", security.addRole(), Role.ADMIN);
            });
        };
    }
}
