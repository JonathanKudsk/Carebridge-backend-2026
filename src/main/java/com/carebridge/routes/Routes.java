package com.carebridge.routes;

import com.carebridge.controllers.impl.JournalEntryController;
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
    private final ChatRoomRoute chatRoomRoute = new ChatRoomRoute();
    private final ChatRoomUserRoute chatRoomUserRoute = new ChatRoomUserRoute();
    private final MessageRoute messageRoute = new MessageRoute();
    private final ShiftRoute shiftRoute = new ShiftRoute();

    public EndpointGroup getRoutes() {
        return () -> {
            path("/users", userRoute.getRoutes());
            path("/event-types", eventTypeRoute.getRoutes());
            path("/events", eventRoute.getRoutes());
            path("/residents", residentRoute.getRoutes());
            path("/journals", journalEntryRoute.getRoutes());
            path("/chatrooms", chatRoomRoute.getRoutes());
            path("/chatroom-users", chatRoomUserRoute.getRoutes());
            path("/messages", messageRoute.getRoutes());
            path("/shifts", shiftRoute.getRoutes());

            get("/populate", controller::populate, Role.ANYONE);
            post("/populate", controller::populate, Role.ANYONE);
            SecurityRoutes.getSecurityRoutes().addEndpoints();
        };
    }
}
