package com.carebridge.controllers.security;

import com.carebridge.dtos.JwtUserDTO;
import io.javalin.http.Handler;
import io.javalin.security.RouteRole;

import java.util.Set;

public interface ISecurityController {
    Handler login();

    Handler register();

    Handler authenticate();

    boolean authorize(JwtUserDTO userDTO, Set<RouteRole> allowedRoles);

    String createToken(JwtUserDTO user) throws Exception;

    JwtUserDTO verifyToken(String token) throws Exception;
}
