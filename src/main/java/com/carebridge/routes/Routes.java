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

    public EndpointGroup getRoutes() {
        return () -> {
            path("/users", userRoute.getRoutes());
            path("/event-types", eventTypeRoute.getRoutes());
            path("/events", eventRoute.getRoutes());

            get("/populate", controller::populate, Role.ANYONE);
            post("/populate", controller::populate, Role.ANYONE);
            SecurityRoutes.getSecurityRoutes().addEndpoints();
        };
    }
}
