package com.carebridge.routes;

import com.carebridge.controllers.impl.ResidentController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ResidentRoute {

    private final ResidentController controller = new ResidentController();

    public EndpointGroup getRoutes() {
        return () -> {
            post("/create", controller::create);
            // Define resident-related routes here
        };
    }
}
