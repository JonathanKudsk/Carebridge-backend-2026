package com.carebridge.routes;

import com.carebridge.controllers.impl.BudgetController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.post;

public class BudgetRoute {

    private final BudgetController controller =
            new BudgetController();

    public EndpointGroup getRoutes() {

        return () -> {

            post(
                    "/create",
                    controller::create,
                    Role.ADMIN,
                    Role.CAREWORKER
            );
        };
    }
}