package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.*;
import com.carebridge.dtos.*;
import com.carebridge.entities.*;
import com.carebridge.exceptions.ApiRuntimeException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JournalEntryController implements IController<JournalEntry, Long> {

    private static final Logger logger = LoggerFactory.getLogger(JournalEntryController.class);
    private final JournalEntryDAO journalEntryDAO = JournalEntryDAO.getInstance();
    private final JournalDAO journalDAO = JournalDAO.getInstance();
    private final TemplateDAO templateDAO = TemplateDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();

    // Get entry details (logic moved from service)
    public void read(Context ctx) {
        try {

            Long entryId = Long.parseLong(ctx.pathParam("entryId"));
            JournalEntry entry = journalEntryDAO.read(entryId);

            if (entry == null) {
                throw new IllegalArgumentException("Journal entry with ID " + entryId + " not found");
            }

            JournalEntryDetailedResponseDTO dto = new JournalEntryDetailedResponseDTO(
                    entry.getId(),
                    entry.getJournal() != null ? entry.getJournal().getId() : null,
                    entry.getAuthor() != null ? entry.getAuthor().getId() : null,
                    entry.getTitle(),
                    entry.getEntryType(),
                    entry.getRiskAssessment(),
                    entry.getCreatedAt(),
                    entry.getUpdatedAt(),
                    entry.getEditCloseTime(),
                    entry.getJournalEntryAnswers().stream().map(JournalEntryAnswerResponseDTO::new).toArray(JournalEntryAnswerResponseDTO[]::new)
            );

            ctx.json(dto);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void readAll(Context ctx) {
        //todo: implementation missing?
    }

    // Finding all entries by a journal ID
    public void findAllEntriesByJournal(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));
            List<Long> ids = journalEntryDAO.getEntryIdsByJournalId(journalId);
            ctx.json(ids);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Internal server error");
        }
    }

    // Create a new journal entry (logic moved from service)
    public void create(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));
            CreateJournalEntryRequestDTO requestDTO = ctx.bodyAsClass(CreateJournalEntryRequestDTO.class);
            requestDTO.setJournalId(journalId);

            //Hentning af User er taget fra EventController
            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof JwtUserDTO ju) email = ju.getUsername();
            else if (tokenUser instanceof com.carebridge.dtos.UserDTO du) email = du.getEmail();
            else if (tokenUser != null) email = tokenUser.toString();

            if (email == null) throw new ApiRuntimeException(401, "Could not find user from token");

            User author = userDAO.readByEmail(email);
            if (author == null) {
                ctx.status(401).json("{\"msg\":\"Author not found\"}");
                return;
            }

            // --- 1. Fetch Journal and Author ---
            Journal journal = journalDAO.read(requestDTO.getJournalId());
            if (journal == null) {
                throw new IllegalArgumentException("Journal not found with ID: " + requestDTO.getJournalId());
            }

            Template template = templateDAO.read(requestDTO.getTemplateId());
            if (template == null) {
                throw new IllegalArgumentException("Template not found with ID: " + requestDTO.getTemplateId());
            }

            // --- 2. Validate Required Input ---
            if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
                throw new IllegalArgumentException("Title is required.");
            }
            if (requestDTO.getEntryType() == null) {
                throw new IllegalArgumentException("Entry type is required.");
            }
            if (requestDTO.getRiskAssessment() == null) {
                throw new IllegalArgumentException("Risk assessment is required.");
            }
            List<Long> fieldids = template.getFields().stream().map(Field::getId).toList();
            for (CreateJournalEntryAnswerRequestDTO answers : requestDTO.getAnswers()) {
                //todo: this doesn't work for some reason
                if (fieldids.contains(answers.getFieldId()) ) { 
                }else {
                    throw new IllegalArgumentException("Fieldid " + answers.getFieldId() + " is invalid");
                }

                //todo: maybe check if data is good too? not sure about how. maybe some util class to do that
            }

            // --- 3. Build entity ---
            LocalDateTime now = LocalDateTime.now();

            JournalEntry entry = new JournalEntry(
                    journal,
                    author,
                    requestDTO.getTitle(),
                    requestDTO.getRiskAssessment(),
                    requestDTO.getEntryType()
            );

            List<JournalEntryAnswer> journalEntryAnswers =
                    Arrays.stream(requestDTO.getAnswers()).
                            map(answer -> {
                                Field field = template.getFields().stream().filter(f -> f.getId().equals(answer.getFieldId())).findAny().get(); //was validified earlier to exist //also filtering everything to then get what is remaining is suboptimal
                                return JournalEntryAnswer.builder().
                                        journalEntry(entry).
                                        answer(answer.getAnswer()).
                                        field(field)
                                        .build();
                            }).
                            toList();
            entry.setJournalEntryAnswers(journalEntryAnswers);

            // --- 4. Persist ---
            journalEntryDAO.create(entry);

            // --- 5. Build response DTO ---
            JournalEntryDetailedResponseDTO responseDTO = new JournalEntryDetailedResponseDTO(
                    entry.getId(),
                    journal.getId(),
                    author.getId(),
                    entry.getTitle(),
                    entry.getEntryType(),
                    entry.getRiskAssessment(),
                    entry.getCreatedAt(),
                    entry.getUpdatedAt(),
                    entry.getEditCloseTime(),
                    entry.getJournalEntryAnswers().stream().map(JournalEntryAnswerResponseDTO::new).toArray(JournalEntryAnswerResponseDTO[]::new)
            );

            ctx.status(201).json(responseDTO);

            // Add entry to journal (only if creation succeeded) //what??? //todo: add to db first instead of this nonsense
            if (ctx.status().getCode() == 201) {
                journalDAO.addEntryToJournal(journal, entry);
            }

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Internal server error");
        }
    }

    // Edit entry content (logic moved from service)
    public void update(Context ctx) { //todo: fix changing answer
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));
            Long entryId = Long.parseLong(ctx.pathParam("entryId"));

            EditJournalEntryRequestDTO requestDTO = ctx.bodyAsClass(EditJournalEntryRequestDTO.class);

            Journal journal = journalDAO.read(journalId);
            if (journal == null) {
                throw new IllegalArgumentException("Journal not found with ID: " + journalId);
            }

            JournalEntry entry = journalEntryDAO.read(entryId);
            if (entry == null) {
                throw new IllegalArgumentException("Journal entry not found with ID: " + entryId);
            }

            if (entry.getJournal() == null || entry.getJournal().getId() == null ||
                    !entry.getJournal().getId().equals(journalId)) {
                throw new IllegalArgumentException("Journal entry does not belong to the specified journal.");
            }

            if (requestDTO.getContent() == null || requestDTO.getContent().isBlank()) {
                throw new IllegalArgumentException("Content is required.");
            }

            LocalDateTime now = LocalDateTime.now();
            if (entry.getEditCloseTime() == null || now.isAfter(entry.getEditCloseTime())) {
                throw new IllegalArgumentException("Edit window has closed for this entry.");
            }

            entry.setUpdatedAt(now);

            journalEntryDAO.update(entryId, entry);

            JournalEntryResponseDTO responseDTO = new JournalEntryResponseDTO(
                    entry.getId(),
                    journal.getId(),
                    entry.getAuthor() != null ? entry.getAuthor().getId() : null,
                    entry.getTitle(),
                    entry.getEntryType(),
                    entry.getRiskAssessment(),
                    entry.getCreatedAt(),
                    entry.getUpdatedAt(),
                    entry.getEditCloseTime()
            );

            ctx.status(200).json(responseDTO);

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void delete(Context ctx) {

    }

    @Override
    public boolean validatePrimaryKey(Long aLong) {
        return false;
    }

    @Override
    public JournalEntry validateEntity(Context ctx) {
        return null;
    }


}