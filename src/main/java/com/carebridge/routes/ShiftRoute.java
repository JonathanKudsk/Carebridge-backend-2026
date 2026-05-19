package com.carebridge.routes;

import com.carebridge.controllers.impl.ShiftController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;


import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.*;

public class ShiftRoute {

    private final ShiftController shiftController = new ShiftController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/by-user", shiftController::readByUser, Role.CAREWORKER, Role.PLANNER, Role.ADMIN);
            post("/", shiftController::create, Role.PLANNER, Role.ADMIN);
            post(shiftController::create, Role.PLANNER, Role.ADMIN);
            path("{id}", () -> {
                put(shiftController::update, Role.PLANNER, Role.ADMIN);
            });
        };
        };
    }
