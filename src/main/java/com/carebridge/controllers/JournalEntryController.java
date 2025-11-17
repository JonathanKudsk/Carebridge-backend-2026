package com.carebridge.controllers;

import com.carebridge.dao.JournalEntryDAO;
import com.carebridge.dao.JournalDAO;
import com.carebridge.dao.UserDAO;
import com.carebridge.dtos.CreateJournalEntryRequestDTO;
import com.carebridge.dtos.EditJournalEntryRequestDTO;
import com.carebridge.dtos.JournalEntryResponseDTO;
import com.carebridge.services.JournalEntryService;
import io.javalin.Javalin;

import java.util.List;

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

        findAllEntriesByJournal(app);
        createJournalEntry(app);
        editJournalEntry(app);
    }

    //Finding all entries by a journal ID
    public void findAllEntriesByJournal(Javalin app) {
        app.get("/journals/{journalId}/entries", ctx -> {
            try {
                Long journalId = Long.parseLong(ctx.pathParam("journalId"));
                ctx.json(service.getEntryIdsForJournal(journalId));
            } catch (IllegalArgumentException e) {
                ctx.status(400).result(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Internal server error");
            }
        });
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

    private void editJournalEntry(Javalin app) {
        app.patch("/journals/{journalId}/journal-entries/{entryId}", ctx -> {
            try {
                Long journalId = Long.parseLong(ctx.pathParam("journalId"));
                Long entryId = Long.parseLong(ctx.pathParam("entryId"));

                EditJournalEntryRequestDTO requestDTO = ctx.bodyAsClass(EditJournalEntryRequestDTO.class);

                JournalEntryResponseDTO responseDTO = service.editJournalEntryContent(journalId, entryId, requestDTO);

                ctx.status(200).json(responseDTO);

            } catch (IllegalArgumentException e) {
                ctx.status(400).result(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Internal server error");
            }
        });
    }

    private void getEntryDetails(Javalin app) {

        // GET /journal-entries/:entryId
        app.get("/journals/{journalId}/journal-entries/{entryId}", ctx -> {
            Long entryId = Long.parseLong(ctx.pathParam("entryId"));
            JournalEntryResponseDTO dto = service.getEntryDetails(entryId);
            ctx.json(dto);
        });
    }

}