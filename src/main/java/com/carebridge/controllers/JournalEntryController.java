package com.carebridge.controllers;

import com.carebridge.dao.JournalEntryDAO;
import com.carebridge.dao.JournalDAO;
import com.carebridge.dao.UserDAO;
import com.carebridge.dtos.CreateJournalEntryRequestDTO;
import com.carebridge.dtos.JournalEntryResponseDTO;
import com.carebridge.services.JournalEntryService;
import io.javalin.Javalin;

public class JournalEntryController {

    private final JournalEntryService service;
    private final JournalEntryDAO jeDAO;
    private final JournalDAO jDAO;

    public JournalEntryController(Javalin app) {
        this.service = new JournalEntryService(
                jeDAO = new JournalEntryDAO(),
                jDAO = new JournalDAO(),
                new UserDAO()
        );

        createJournalEntry(app);
    }

    //Possibly move the app.post-part to routes later
    private void createJournalEntry(Javalin app) {
        app.post("/journals/{journalId}/journal-entries", ctx -> {
            try {
                Long journalId = Long.parseLong(ctx.pathParam("journalId"));

                CreateJournalEntryRequestDTO requestDTO = ctx.bodyAsClass(CreateJournalEntryRequestDTO.class);
                requestDTO.setJournalId(journalId);
                //TODO: Should be changed so its not hardcoded
                requestDTO.setAuthorUserId(3L); // in real app, from auth

                JournalEntryResponseDTO responseDTO = service.createJournalEntry(requestDTO);

                ctx.status(201).json(responseDTO);

                // Add entry to journal -> But only if creation was successful
                if (ctx.status().getCode() == 201) {
                    jDAO.addEntryToJournal(journalId, responseDTO.getId());
                }

            } catch (IllegalArgumentException e) {
                ctx.status(400).result(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Internal server error");
            }
        });
    }
}