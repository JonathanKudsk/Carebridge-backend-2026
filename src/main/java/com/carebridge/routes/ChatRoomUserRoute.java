package com.carebridge.routes;

import com.carebridge.controllers.impl.ChatRoomUserController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ChatRoomUserRoute {

    private final ChatRoomUserController controller = new ChatRoomUserController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", controller::readAll, Role.USER, Role.ADMIN);
            get("/{id}", controller::read, Role.USER, Role.ADMIN);
            post("/", controller::create, Role.USER, Role.ADMIN);
            put("/{id}", controller::update, Role.USER, Role.ADMIN);
            delete("/{id}", controller::delete, Role.USER, Role.ADMIN);
        };
    }
}

