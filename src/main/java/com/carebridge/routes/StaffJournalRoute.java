package com.carebridge.routes;

import com.carebridge.controllers.impl.StaffJournalController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class StaffJournalRoute {

    private final StaffJournalController controller = new StaffJournalController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("/user/{userId}",                        controller::getByUserId,      Role.ADMIN);
            post("/{id}/contract",                       controller::uploadContract,   Role.ADMIN);
            get("/{id}/contract/{contractId}",           controller::downloadContract, Role.ADMIN);
            delete("/{id}/contract/{contractId}",        controller::deleteContract,   Role.ADMIN);
            get("/{id}/entries",                         controller::getEntries,       Role.ADMIN);
            post("/{id}/entries",                        controller::createEntry,      Role.ADMIN);
            put("/{id}/entries/{entryId}",               controller::updateEntry,      Role.ADMIN);
            delete("/{id}/entries/{entryId}",            controller::deleteEntry,      Role.ADMIN);
        };
    }
}
