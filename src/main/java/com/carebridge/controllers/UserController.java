package com.carebridge.controllers;

import io.javalin.Javalin;
import com.carebridge.services.UserService;
import com.carebridge.models.User;
import java.util.List;

public class UserController {
    private final UserService userService = new UserService();

    public UserController(Javalin app) {
        app.get("/users", ctx -> {
            List<User> users = userService.getAllUsers();
            ctx.json(users);
        });

        app.post("/users", ctx -> {
            User user = ctx.bodyAsClass(User.class);
            userService.saveUser(user);
            ctx.status(201).json(user);
        });
    }
}
