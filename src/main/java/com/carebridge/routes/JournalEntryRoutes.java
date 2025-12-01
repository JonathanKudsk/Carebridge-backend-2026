package com.carebridge.routes;

import com.carebridge.controllers.impl.JournalEntryController;
import com.carebridge.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class JournalEntryRoutes
{
    private final JournalEntryController controller = new JournalEntryController();

    public EndpointGroup getRoutes() {
        return () ->
        {
            get("/{journalId}/journal-entries", controller::findAllEntriesByJournal, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
            post("/{journalId}/journal-entries", controller::create, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
            put("/{journalId}/journal-entries/{entryId}", controller::update, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
            get("/{journalId}/journal-entries/{entryId}", controller::read, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
        };
    }
}