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
            get("/journals/{journalId}/journal-entries", controller::findAllEntriesByJournal);
            post("/journals/{journalId}/journal-entries", controller::create);
            put("/journals/{journalId}/journal-entries/{entryId}", controller::update);
            get("/journals/{journalId}/journal-entries/{entryId}", controller::read);
        };
    }
}