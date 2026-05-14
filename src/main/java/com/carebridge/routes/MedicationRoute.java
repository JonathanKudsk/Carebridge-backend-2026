package com.carebridge.routes;

import com.carebridge.controllers.impl.MedicationController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MedicationRoute {

    private final MedicationController controller = new MedicationController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/{chartId}", controller::readAll, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
            post("/{chartId}/medications", controller::create, Role.ADMIN, Role.CAREWORKER);
            get("/{chartId}/medications/{medicationId}", controller::read, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
            put("/{chartId}/medications/{medicationId}", controller::update, Role.ADMIN, Role.CAREWORKER);
            delete("/{chartId}/medications/{medicationId}", controller::delete, Role.ADMIN);
        };
    }
}
