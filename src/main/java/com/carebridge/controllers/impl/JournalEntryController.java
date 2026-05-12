package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.JournalDAO;
import com.carebridge.dao.impl.JournalEntryDAO;
import com.carebridge.dao.impl.TemplateDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.CreateJournalEntryAnswerRequestDTO;
import com.carebridge.dtos.CreateJournalEntryRequestDTO;
import com.carebridge.dtos.EditJournalEntryRequestDTO;
import com.carebridge.dtos.JournalEntryAnswerResponseDTO;
import com.carebridge.dtos.JournalEntryDetailedResponseDTO;
import com.carebridge.dtos.JournalEntryResponseDTO;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.entities.Field;
import com.carebridge.entities.Journal;
import com.carebridge.entities.JournalEntry;
import com.carebridge.entities.JournalEntryAnswer;
import com.carebridge.entities.Template;
import com.carebridge.entities.User;
import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import com.carebridge.exceptions.ApiRuntimeException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JournalEntryController implements IController<JournalEntry, Long> {

    private static final Logger logger = LoggerFactory.getLogger(JournalEntryController.class);
    private final JournalEntryDAO journalEntryDAO = JournalEntryDAO.getInstance();
    private final JournalDAO journalDAO = JournalDAO.getInstance();
    private final TemplateDAO templateDAO = TemplateDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();

    // Admins and careworkers can access all journals, while guardians can only
    // access journals belonging to residents they are linked to.
    private boolean canAccessJournal(Context ctx, Long journalId) {
        var tokenUser = ctx.attribute("user");

        if (!(tokenUser instanceof JwtUserDTO jwtUser)) {
            ctx.status(401).json("{\"msg\":\"Unauthorized\"}");
            return false;
        }

        boolean isAdminOrCareworker = jwtUser.getRoles().stream()
                .map(String::toUpperCase)
                .anyMatch(role -> role.equals("ADMIN") || role.equals("CAREWORKER"));

        if (isAdminOrCareworker) {
            return true;
        }

        boolean isGuardian = jwtUser.getRoles().stream()
                .map(String::toUpperCase)
                .anyMatch(role -> role.equals("GUARDIAN"));

        if (isGuardian && journalDAO.guardianHasAccessToJournal(jwtUser.getUsername(), journalId)) {
            return true;
        }

        ctx.status(403).json("{\"msg\":\"Forbidden\"}");
        return false;
    }

    @Override
    public void read(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));
            Long entryId = Long.parseLong(ctx.pathParam("entryId"));

            if (!canAccessJournal(ctx, journalId)) {
                return;
            }

            JournalEntry entry = journalEntryDAO.read(entryId);
            if (entry == null) {
                throw new IllegalArgumentException("Journal entry with ID " + entryId + " not found");
            }

            if (entry.getJournal() == null || !entry.getJournal().getId().equals(journalId)) {
                ctx.status(404).json("{\"msg\":\"Journal entry not found in this journal\"}");
                return;
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
                    entry.getJournalEntryAnswers().stream()
                            .map(JournalEntryAnswerResponseDTO::new)
                            .toArray(JournalEntryAnswerResponseDTO[]::new)
            );

            ctx.json(dto);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void readAll(Context ctx) {
    }

    public void findAllEntriesByJournal(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));

            if (!canAccessJournal(ctx, journalId)) {
                return;
            }

            List<Long> ids = journalEntryDAO.getEntryIdsByJournalId(journalId);
            ctx.json(ids);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to find journal entry ids", e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void create(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));
            CreateJournalEntryRequestDTO requestDTO = ctx.bodyAsClass(CreateJournalEntryRequestDTO.class);
            requestDTO.setJournalId(journalId);

            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof JwtUserDTO ju) {
                email = ju.getUsername();
            } else if (tokenUser instanceof com.carebridge.dtos.UserDTO du) {
                email = du.getEmail();
            } else if (tokenUser != null) {
                email = tokenUser.toString();
            }

            if (email == null) {
                throw new ApiRuntimeException(401, "Could not find user from token");
            }

            User author = userDAO.readByEmail(email);
            if (author == null) {
                ctx.status(401).json("{\"msg\":\"Author not found\"}");
                return;
            }

            Journal journal = journalDAO.read(requestDTO.getJournalId());
            if (journal == null) {
                throw new IllegalArgumentException("Journal not found with ID: " + requestDTO.getJournalId());
            }

            Template template = templateDAO.read(requestDTO.getTemplateId());
            if (template == null) {
                throw new IllegalArgumentException("Template not found with ID: " + requestDTO.getTemplateId());
            }

            if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
                throw new IllegalArgumentException("Title is required.");
            }
            if (requestDTO.getEntryType() == null) {
                throw new IllegalArgumentException("Entry type is required.");
            }
            if (requestDTO.getRiskAssessment() == null) {
                throw new IllegalArgumentException("Risk assessment is required.");
            }

            List<Long> fieldIds = template.getFields().stream().map(Field::getId).toList();
            for (CreateJournalEntryAnswerRequestDTO answers : requestDTO.getAnswers()) {
                boolean isMatch = false;
                for (Long id : fieldIds) {
                    if (id.equals(answers.getFieldId())) {
                        isMatch = true;
                        break;
                    }
                }
                if (!isMatch) {
                    throw new IllegalArgumentException("Fieldid " + answers.getFieldId() + " is invalid");
                }
            }

            JournalEntry entry = new JournalEntry(
                    journal,
                    author,
                    requestDTO.getTitle(),
                    requestDTO.getRiskAssessment(),
                    requestDTO.getEntryType(),
                    template
            );

            List<JournalEntryAnswer> journalEntryAnswers = Arrays.stream(requestDTO.getAnswers())
                    .map(answer -> {
                        Field field = template.getFields().stream()
                                .filter(f -> f.getId().equals(answer.getFieldId()))
                                .findAny()
                                .orElseThrow();
                        return JournalEntryAnswer.builder()
                                .journalEntry(entry)
                                .answer(answer.getAnswer())
                                .field(field)
                                .build();
                    })
                    .toList();
            entry.setJournalEntryAnswers(journalEntryAnswers);

            journalEntryDAO.create(entry);

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
                    entry.getJournalEntryAnswers().stream()
                            .map(JournalEntryAnswerResponseDTO::new)
                            .toArray(JournalEntryAnswerResponseDTO[]::new)
            );

            ctx.status(201).json(responseDTO);

            if (ctx.status().getCode() == 201) {
                journalDAO.addEntryToJournal(journal, entry);
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create journal entry", e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void update(Context ctx) {
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

            if (entry.getJournal() == null || !entry.getJournal().getId().equals(journalId)) {
                throw new IllegalArgumentException("Journal entry does not belong to the specified journal.");
            }

            LocalDateTime now = LocalDateTime.now();
            if (entry.getEditCloseTime() == null || now.isAfter(entry.getEditCloseTime())) {
                throw new IllegalArgumentException("Edit window has closed for this entry.");
            }

            if (requestDTO.getTitle() != null && !requestDTO.getTitle().isBlank()) {
                entry.setTitle(requestDTO.getTitle());
            }

            if (requestDTO.getRiskAssessment() != null) {
                try {
                    entry.setRiskAssessment(RiskAssessment.valueOf(requestDTO.getRiskAssessment().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid riskAssessment: " + requestDTO.getRiskAssessment());
                }
            }

            if (requestDTO.getEntryType() != null) {
                try {
                    entry.setEntryType(EntryType.valueOf(requestDTO.getEntryType().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid entryType: " + requestDTO.getEntryType());
                }
            }

            if (requestDTO.getAnswers() != null) {
                var existingAnswers = entry.getJournalEntryAnswers().stream()
                        .collect(Collectors.toMap(a -> a.getField().getId(), a -> a));

                for (CreateJournalEntryAnswerRequestDTO incoming : requestDTO.getAnswers()) {
                    JournalEntryAnswer existing = existingAnswers.get(incoming.getFieldId());

                    if (existing == null) {
                        throw new IllegalArgumentException("Invalid fieldId: " + incoming.getFieldId());
                    }

                    existing.setAnswer(incoming.getAnswer());
                }
            }

            entry.setUpdatedAt(now);
            journalEntryDAO.update(entryId, entry);

            JournalEntryDetailedResponseDTO responseDTO = new JournalEntryDetailedResponseDTO(
                    entry.getId(),
                    journal.getId(),
                    entry.getAuthor() != null ? entry.getAuthor().getId() : null,
                    entry.getTitle(),
                    entry.getEntryType(),
                    entry.getRiskAssessment(),
                    entry.getCreatedAt(),
                    entry.getUpdatedAt(),
                    entry.getEditCloseTime(),
                    entry.getJournalEntryAnswers().stream()
                            .map(JournalEntryAnswerResponseDTO::new)
                            .toArray(JournalEntryAnswerResponseDTO[]::new)
            );

            ctx.status(200).json(responseDTO);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update journal entry", e);
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

    public void findAllEntriesByJournalToDTO(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));

            if (!canAccessJournal(ctx, journalId)) {
                return;
            }

            List<JournalEntry> entries = journalEntryDAO.getEntriesByJournalId(journalId);
            List<JournalEntryResponseDTO> dtos = entries.stream()
                    .map(entry -> new JournalEntryResponseDTO(
                            entry.getId(),
                            entry.getJournal().getId(),
                            entry.getAuthor().getId(),
                            entry.getTitle(),
                            entry.getEntryType(),
                            entry.getRiskAssessment(),
                            entry.getCreatedAt(),
                            entry.getUpdatedAt(),
                            entry.getEditCloseTime()
                    ))
                    .toList();
            ctx.json(dtos);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to fetch journal entries", e);
            ctx.status(500).result("Internal server error");
        }
    }
}
