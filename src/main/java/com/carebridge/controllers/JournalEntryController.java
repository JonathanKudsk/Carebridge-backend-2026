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

    public JournalEntryController(Javalin app) {
        this.service = new JournalEntryService(
                new JournalEntryDAO(),
                new JournalDAO(),
                new UserDAO()
        );

        // Register endpoint(s)
        app.post("/residents/{residentId}/journal-entries", ctx -> {
            try {
                Long residentId = Long.parseLong(ctx.pathParam("residentId"));

                // Parse JSON request → DTO
                CreateJournalEntryRequestDTO requestDTO = ctx.bodyAsClass(CreateJournalEntryRequestDTO.class);
                requestDTO.setResidentId(residentId);

                // In real app, authorUserId would come from authentication
                requestDTO.setAuthorUserId(1L);

                // Call service
                JournalEntryResponseDTO responseDTO = service.createJournalEntry(requestDTO);

                ctx.status(201).json(responseDTO);
            } catch (IllegalArgumentException e) {
                ctx.status(400).result(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Internal server error");
            }
        });
    }
}
