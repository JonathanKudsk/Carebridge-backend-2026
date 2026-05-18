package com.carebridge.routes;

import com.carebridge.controllers.impl.ShiftController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ShiftRoute {

    private final ShiftController shiftController = new ShiftController();

    public EndpointGroup getRoutes() {
        return () -> {
            post("/", shiftController::create, Role.PLANNER, Role.ADMIN);
            post(shiftController::create, Role.PLANNER, Role.ADMIN);
            path("{id}", () -> {
                put(shiftController::update, Role.PLANNER, Role.ADMIN);
            });
        };
        };
    }
