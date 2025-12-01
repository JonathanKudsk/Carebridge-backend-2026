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
            get("/{journalId}/journal-entries", controller::findAllEntriesByJournal, Role.USER, Role.ADMIN);
            post("/{journalId}/journal-entries", controller::create, Role.USER, Role.ADMIN);
            put("/{journalId}/journal-entries/{entryId}", controller::update, Role.USER, Role.ADMIN);
            get("/{journalId}/journal-entries/{entryId}", controller::read, Role.USER, Role.ADMIN);

        };
    }
}