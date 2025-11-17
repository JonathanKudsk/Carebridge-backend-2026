package com.carebridge.routes;

import com.carebridge.controllers.impl.EventController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class EventRoute {
    private final EventController controller = new EventController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::readAll, Role.USER);
            get("/upcoming", controller::readUpcoming, Role.USER);
            get("/creator/{userId}", controller::readByCreator, Role.USER);

            get("/{id}", controller::read, Role.USER);

            post("/", controller::create, Role.USER);
            put("/{id}", controller::update, Role.USER);

            delete("/{id}", controller::delete, Role.ADMIN, Role.USER);

        };
    }
}
