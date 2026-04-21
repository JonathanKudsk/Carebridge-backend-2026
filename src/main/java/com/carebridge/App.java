package com.carebridge;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {

        Javalin app = ApplicationConfig.startServer(7070);

        app.get("/", ctx -> ctx.result("Carebridge API is running"));

        User user1 = new User("admin", "admin@gmail.com", "1234", Role.ADMIN);

        UserDAO userDAO = UserDAO.getInstance();
        userDAO.create(user1);
    }
}
