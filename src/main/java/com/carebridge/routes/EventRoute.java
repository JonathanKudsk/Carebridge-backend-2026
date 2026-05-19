package com.carebridge.routes;

import com.carebridge.controllers.impl.EventController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class EventRoute {
    private final EventController controller = new EventController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::readAll, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);
            get("/upcoming", controller::readUpcoming, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);
            get("/creator/{userId}", controller::readByCreator, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);

            get("/{id}", controller::read, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);

            post("/", controller::create, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);
            put("/{id}", controller::update, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);

            delete("/{id}", controller::delete, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);
            post("/{id}/mark-seen", controller::markSeen, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);
            delete("/{id}/mark-seen", controller::unmarkSeen, Role.CAREWORKER, Role.GUARDIAN, Role.ADMIN, Role.SUBSTITUTE);
        };
    }
}
