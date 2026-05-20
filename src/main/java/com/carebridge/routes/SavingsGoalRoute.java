package com.carebridge.routes;

import com.carebridge.controllers.impl.SavingsGoalController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SavingsGoalRoute {
    private final SavingsGoalController controller = new SavingsGoalController();

    public EndpointGroup getRoutes() {
        return () -> {
            post("/", controller::create, Role.CAREWORKER, Role.ADMIN);
            get("/", controller::readAll, Role.CAREWORKER, Role.ADMIN);
            get("/resident/{residentId}", controller::getByResident, Role.CAREWORKER, Role.ADMIN);
            get("/{id}", controller::read, Role.CAREWORKER, Role.ADMIN);
            put("/{id}", controller::update, Role.CAREWORKER, Role.ADMIN);
            delete("/{id}", controller::delete, Role.CAREWORKER, Role.ADMIN);
        };
    }
}
