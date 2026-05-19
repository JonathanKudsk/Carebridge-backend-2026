package com.carebridge.routes;

import com.carebridge.controllers.impl.ShiftAssignmentController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.post;

public class ShiftAssignmentRoute {

    private final ShiftAssignmentController shiftAssignmentController = new ShiftAssignmentController();

    public EndpointGroup getRoutes() {
        return () -> {
            post("/", shiftAssignmentController::create, Role.PLANNER, Role.ADMIN);
        };
    }
}

