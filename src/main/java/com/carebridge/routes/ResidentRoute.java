package com.carebridge.routes;

import com.carebridge.controllers.impl.ResidentController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ResidentRoute {

    private final ResidentController controller = new ResidentController();


    public EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::readAll, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
            post("/create", controller::create, Role.ADMIN);
            // Define resident-related routes here
            get("/sorted", controller::getAllSorted, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
        };
    }
}
