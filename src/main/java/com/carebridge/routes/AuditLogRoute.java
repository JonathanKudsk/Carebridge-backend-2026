package com.carebridge.routes;

import com.carebridge.controllers.impl.AuditLogController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AuditLogRoute {

    private final AuditLogController controller = new AuditLogController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::readAll, Role.ADMIN);
            get("/medication/{medicationId}", controller::readByMedication, Role.ADMIN, Role.CAREWORKER);
        };
    }
}
