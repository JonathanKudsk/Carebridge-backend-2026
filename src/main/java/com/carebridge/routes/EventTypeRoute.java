package com.carebridge.routes;

import com.carebridge.controllers.impl.EventTypeController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class EventTypeRoute {
    private final EventTypeController controller = new EventTypeController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::readAll, Role.ANYONE);
            get("/{id}", controller::read, Role.ANYONE);

            post("/", controller::create, Role.ADMIN);
            put("/{id}", controller::update, Role.ADMIN);
            delete("/{id}", controller::delete, Role.ADMIN);
        };
    }
}
