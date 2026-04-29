package com.carebridge.routes;

import com.carebridge.controllers.impl.UserController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {
    private final UserRoute userRoute = new UserRoute();
    private final EventTypeRoute eventTypeRoute = new EventTypeRoute();
    private final EventRoute eventRoute = new EventRoute();
    private final UserController controller = new UserController();
    private final JournalEntryRoutes journalEntryRoute = new JournalEntryRoutes();
    private final ResidentRoute residentRoute = new ResidentRoute();
    private final MedicationRoute medicationRoute = new MedicationRoute();
    private final AuditLogRoute auditLogRoute = new AuditLogRoute();

    public EndpointGroup getRoutes() {
        return () -> {
            path("/users", userRoute.getRoutes());
            path("/event-types", eventTypeRoute.getRoutes());
            path("/events", eventRoute.getRoutes());
            path("/residents", residentRoute.getRoutes());
            path("/journals", journalEntryRoute.getRoutes());
            path("/medication-charts", medicationRoute.getRoutes());
            path("/audit-logs", auditLogRoute.getRoutes());

            get("/populate", controller::populate, Role.ANYONE);
            post("/populate", controller::populate, Role.ANYONE);
            SecurityRoutes.getSecurityRoutes().addEndpoints();
        };
    }
}
