package com.carebridge;

import com.carebridge.populator.DataPopulator;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;

import com.carebridge.controllers.UserController;
import com.carebridge.controllers.JournalEntryController;

public class App {
    public static void main(String[] args) {

        // --- Initialize Hibernate + Populate Test Data ---
        //DataPopulator.populate();

        Javalin app = Javalin.create(config -> {
            // Aktiver CORS for alle hosts
            config.registerPlugin(new CorsPlugin(cors -> {
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            }));
        }).start(7070);

        // Simpel route
        app.get("/", ctx -> ctx.result("Carebridge API is running ✅"));

        // Example route group
        new UserController(app);
        new JournalEntryController(app);
    }
}
