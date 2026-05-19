package com.carebridge.routes;

import com.carebridge.controllers.impl.PlanPeriodController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class PlanPeriodRoute {

    private final PlanPeriodController controller = new PlanPeriodController();

    public EndpointGroup getRoutes() {
        return () -> {
            post("/", controller::create, Role.PLANNER, Role.ADMIN);
            get("/", controller::readAll, Role.USER, Role.CAREWORKER, Role.GUARDIAN, Role.PLANNER, Role.ADMIN);
            get("/{id}", controller::read, Role.USER, Role.CAREWORKER, Role.GUARDIAN, Role.PLANNER, Role.ADMIN);
        };
    }
}
