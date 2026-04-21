package com.carebridge;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {


        Javalin app = ApplicationConfig.startServer(7070);

        Populator.populate(HibernateConfig.getEntityManagerFactory());

        app.get("/", ctx -> ctx.result("Carebridge API is running"));
    }
}
