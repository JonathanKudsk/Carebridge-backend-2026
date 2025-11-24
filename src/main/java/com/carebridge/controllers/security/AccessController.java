package com.carebridge.controllers.security;

import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.entities.enums.Role;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

import java.util.Set;
import java.util.stream.Collectors;

public class AccessController implements IAccessController {

    SecurityController securityController = SecurityController.getInstance();

    @Override
    public void accessHandler(Context ctx) {
        Set<RouteRole> allowed = ctx.routeRoles();
        System.out.println("ACCESS DEBUG → " + ctx.method() + " " + ctx.path() + " roles=" + allowed);
        if (allowed.isEmpty() || allowed.contains(Role.ANYONE)) return;

        try {
            securityController.authenticate().handle(ctx);
        } catch (UnauthorizedResponse e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedResponse("You need to log in, dude! Or your token is invalid.");
        }

        JwtUserDTO user = ctx.attribute("user");
        if (user == null) throw new UnauthorizedResponse("Unauthorized");

        var allowedNames = allowed.stream()
                .map(Object::toString)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        boolean ok = user.getRoles().stream()
                .map(String::toUpperCase)
                .anyMatch(allowedNames::contains);

        if (!ok) throw new UnauthorizedResponse("Forbidden. Needed roles: " + allowedNames);
    }
}
