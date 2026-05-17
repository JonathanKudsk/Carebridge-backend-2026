package com.carebridge.routes;

import com.carebridge.controllers.impl.LocationController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class LocationRoute {
    private final LocationController controller = new LocationController();

    public EndpointGroup getRoutes() {
        return () ->
        {
            get("/", controller::readAll, Role.ANYONE);
            post("/", controller::create, Role.ADMIN);
            get("/{id}", controller::read, Role.ANYONE);
            post("/{id}", controller::update, Role.ADMIN);
            delete("/{id}", controller::delete, Role.ADMIN);
        };
    }
}