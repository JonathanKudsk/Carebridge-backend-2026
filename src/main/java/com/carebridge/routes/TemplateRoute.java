package com.carebridge.routes;

import com.carebridge.config.Populator;
import com.carebridge.controllers.impl.JournalEntryController;
import com.carebridge.controllers.impl.TemplateController;
import com.carebridge.entities.Template;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.get;

public class TemplateRoute {
    private final TemplateController controller = new TemplateController();

    public EndpointGroup getRoutes() {
        return () ->
        {
            get("/", controller::readAll, Role.ANYONE);
            post("/", controller::create, Role.ADMIN);
            get("/{id}", controller::read, Role.ANYONE);
            delete("/{id}", controller::delete, Role.ADMIN);
        };
    }
}
