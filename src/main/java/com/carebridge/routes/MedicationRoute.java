package com.carebridge.routes;

import com.carebridge.controllers.impl.MedicationController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MedicationRoute {

    private final MedicationController controller = new MedicationController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/{chartId}", controller::readAll, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN, Role.SUBSTITUTE);
            post("/{chartId}/medications", controller::create, Role.ADMIN, Role.CAREWORKER, Role.SUBSTITUTE);
            get("/{chartId}/medications/{medicationId}", controller::read, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN, Role.SUBSTITUTE);
            put("/{chartId}/medications/{medicationId}", controller::update, Role.ADMIN, Role.CAREWORKER, Role.SUBSTITUTE);
            delete("/{chartId}/medications/{medicationId}", controller::delete, Role.ADMIN);
        };
    }
}
