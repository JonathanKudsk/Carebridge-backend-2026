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
<<<<<<< HEAD
    private final DosageRoute dosageRoute = new DosageRoute();
    private final MedicationRoute medicationRoute = new MedicationRoute();
    private final AuditLogRoute auditLogRoute = new AuditLogRoute();
    private final ChatRoomRoutes chatRoomRoute = new ChatRoomRoutes();
    private final ChatRoomUserRoute chatRoomUserRoute = new ChatRoomUserRoute();
    private final MessageRoute messageRoute = new MessageRoute();
    private final ShiftRoute shiftRoute = new ShiftRoute();
=======
    private final TemplateRoute templateRoute = new TemplateRoute();
>>>>>>> TEAM-3

    public EndpointGroup getRoutes() {
        return () -> {
            path("/users", userRoute.getRoutes());
            path("/event-types", eventTypeRoute.getRoutes());
            path("/events", eventRoute.getRoutes());
            path("/residents", residentRoute.getRoutes());
            path("/journals", journalEntryRoute.getRoutes());
<<<<<<< HEAD
            path("/dosages", dosageRoute.getRoutes());
            path("/medication-charts", medicationRoute.getRoutes());
            path("/audit-logs", auditLogRoute.getRoutes());
            path("/chatrooms", chatRoomRoute.getRoutes());
            path("/chatroom-users", chatRoomUserRoute.getRoutes());
            path("/messages", messageRoute.getRoutes());
            path("/shifts", shiftRoute.getRoutes());

=======
            path("/templates",templateRoute.getRoutes());
>>>>>>> TEAM-3
            get("/populate", controller::populate, Role.ANYONE);
            post("/populate", controller::populate, Role.ANYONE);
            SecurityRoutes.getSecurityRoutes().addEndpoints();
        };
    }
}
