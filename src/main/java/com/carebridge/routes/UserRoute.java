package com.carebridge.routes;

import com.carebridge.controllers.impl.UserController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UserRoute {
    private final UserController controller = new UserController();

    public EndpointGroup getRoutes() {
        return () -> {
          
            get("/careworkers", controller::readAllCareWorkers, Role.PLANNER, Role.ADMIN);
            get("/substitutes/locations", controller::readSubstituteLocations, Role.PLANNER, Role.ADMIN);
            get("/substitutes", controller::readAllSubstitutes, Role.PLANNER, Role.ADMIN);

            get("/", controller::readAll, Role.CAREWORKER, Role.ADMIN, Role.SUBSTITUTE);
            get("/{id}", controller::read, Role.CAREWORKER, Role.ADMIN, Role.SUBSTITUTE);


            post("/", controller::create, Role.ADMIN);
            put("/{id}", controller::update, Role.ADMIN);
            delete("/{id}", controller::delete, Role.ADMIN);
            post("/{id}/link-residents", controller::linkResidents, Role.ADMIN);
            get("/by-email/{email}",controller::readByEmail, Role.ADMIN);


            get("/{id}/locations", controller::readWithLocation,Role.CAREWORKER, Role.SUBSTITUTE, Role.PLANNER, Role.ADMIN);
            put("/{userId}/locations/{locationId}", controller::attachUserLocation, Role.ADMIN);
            delete("/{userId}/locations/{locationId}", controller::detachUserLocation, Role.ADMIN);
        };
    }
}
