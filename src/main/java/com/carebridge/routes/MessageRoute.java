package com.carebridge.routes;

import com.carebridge.controllers.impl.MessageController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MessageRoute {

    private final MessageController controller = new MessageController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::readAll, Role.USER, Role.CAREWORKER, Role.ADMIN);
            get("/{id}", controller::read, Role.USER, Role.CAREWORKER, Role.ADMIN);
            post("/", controller::create, Role.USER, Role.CAREWORKER, Role.ADMIN);
            put("/{id}", controller::update, Role.USER, Role.CAREWORKER, Role.ADMIN);
            delete("/{id}", controller::delete, Role.USER, Role.CAREWORKER, Role.ADMIN);
        };
    }
}

