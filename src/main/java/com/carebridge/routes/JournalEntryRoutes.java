package com.carebridge.routes;

import com.carebridge.controllers.impl.JournalEntryController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class JournalEntryRoutes
{
    private final JournalEntryController controller = new JournalEntryController();

    public EndpointGroup getRoutes() {
        return () ->
        {
            get("/{journalId}/journal-entries", controller::findAllEntriesByJournal);
            post("/{journalId}/journal-entries", controller::create);
            put("/{journalId}/journal-entries/{entryId}", controller::update);
            get("/{journalId}/journal-entries/{entryId}", controller::read);
        };
    }
}