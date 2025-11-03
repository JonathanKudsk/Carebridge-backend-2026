package com.carebridge;

import com.carebridge.config.ApplicationConfig;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {

        Javalin app = ApplicationConfig.startServer(7070);

        app.get("/", ctx -> ctx.result("Carebridge API is running"));
    }
}
