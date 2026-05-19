package com.carebridge.routes;

import com.carebridge.controllers.impl.HandbookController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class HandbookRoute {

    private final HandbookController controller = new HandbookController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::getHandbook, Role.CAREWORKER, Role.ADMIN);
            post("/tabs", controller::createTab, Role.ADMIN);
            put("/tabs/{tabId}/content", controller::updateTabContent, Role.ADMIN);
            put("/tabs/{tabId}/title", controller::updateTabTitle, Role.ADMIN);
            delete("/tabs/{tabId}", controller::deleteTab, Role.ADMIN);
        };
    }
}