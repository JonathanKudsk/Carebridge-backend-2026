package com.carebridge.routes;

import com.carebridge.controllers.impl.DosageController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class DosageRoute {
    private final DosageController controller = new DosageController();

    public EndpointGroup getRoutes() {
        return () -> {
            post("/", controller::create, Role.ADMIN);
            put("/{id}", controller::update, Role.ADMIN);
            delete("/{id}", controller::delete, Role.ADMIN);
            get("/{residentId}/dosages", controller::readAllByResident, Role.ADMIN, Role.CAREWORKER);
        };
    }
}