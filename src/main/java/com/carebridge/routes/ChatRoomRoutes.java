package com.carebridge.routes;


import com.carebridge.controllers.impl.ChatRoomController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;


public class ChatRoomRoutes {
    private final ChatRoomController controller = new ChatRoomController();

    public EndpointGroup getRoutes(){
        return () -> {
            get("/", controller::readAll, Role.ADMIN);
            get("/{id}", controller::read, Role.ADMIN);
            post("/", controller::create, Role.ADMIN);
            put("/{id}", controller::update, Role.ADMIN);
            delete("/{id}", controller::delete, Role.ADMIN);
        };
    }
}
