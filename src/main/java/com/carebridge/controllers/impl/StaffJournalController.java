package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.StaffJournalContractDAO;
import com.carebridge.dao.impl.StaffJournalDAO;
import com.carebridge.dao.impl.StaffJournalEntryDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.CreateStaffJournalEntryRequestDTO;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.StaffJournalContractDTO;
import com.carebridge.dtos.StaffJournalEntryResponseDTO;
import com.carebridge.dtos.StaffJournalResponseDTO;
import com.carebridge.dtos.UpdateStaffJournalEntryRequestDTO;
import com.carebridge.entities.StaffJournal;
import com.carebridge.entities.StaffJournalContract;
import com.carebridge.entities.StaffJournalEntry;
import com.carebridge.entities.User;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class StaffJournalController {

    private static final Logger logger = LoggerFactory.getLogger(StaffJournalController.class);
    private static final String UPLOAD_DIR = "uploads/contracts/";

    private final StaffJournalDAO staffJournalDAO = StaffJournalDAO.getInstance();
    private final StaffJournalEntryDAO entryDAO = StaffJournalEntryDAO.getInstance();
    private final StaffJournalContractDAO contractDAO = StaffJournalContractDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();

    public void getByUserId(Context ctx) {
        try {
            Long userId = Long.parseLong(ctx.pathParam("userId"));
            StaffJournal journal = staffJournalDAO.readByUserId(userId);
            if (journal == null) {
                ctx.status(404).json("{\"msg\":\"Staff journal not found for user\"}");
                return;
            }
            List<StaffJournalEntry> entries = entryDAO.readAllByJournalId(journal.getId());
            List<StaffJournalContract> contracts = contractDAO.readAllByJournalId(journal.getId());
            ctx.json(toResponseDTO(journal, entries, contracts));
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\":\"Invalid user ID\"}");
        } catch (Exception e) {
            logger.error("getByUserId failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void uploadContract(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("id"));
            StaffJournal journal = staffJournalDAO.read(journalId);
            if (journal == null) {
                ctx.status(404).json("{\"msg\":\"Staff journal not found\"}");
                return;
            }

            var uploadedFile = ctx.uploadedFile("file");
            if (uploadedFile == null) {
                ctx.status(400).json("{\"msg\":\"No file uploaded\"}");
                return;
            }
            if (!"application/pdf".equals(uploadedFile.contentType())) {
                ctx.status(400).json("{\"msg\":\"Only PDF files are allowed\"}");
                return;
            }

            Path uploadDir = Path.of(UPLOAD_DIR);
            Files.createDirectories(uploadDir);

            String originalFilename = uploadedFile.filename();
            String storedFilename = journalId + "_" + System.currentTimeMillis() + "_" + originalFilename;
            Path destination = uploadDir.resolve(storedFilename);

            try (InputStream content = uploadedFile.content()) {
                Files.copy(content, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            StaffJournalContract contract = new StaffJournalContract();
            contract.setStaffJournal(journal);
            contract.setFilename(originalFilename);
            contract.setPath(destination.toString());

            StaffJournalContract created = contractDAO.create(contract);
            ctx.status(201).json(toContractDTO(created));
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\":\"Invalid journal ID\"}");
        } catch (Exception e) {
            logger.error("uploadContract failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void downloadContract(Context ctx) {
        try {
            Long contractId = Long.parseLong(ctx.pathParam("contractId"));
            StaffJournalContract contract = contractDAO.read(contractId);
            if (contract == null) {
                ctx.status(404).json("{\"msg\":\"Contract not found\"}");
                return;
            }

            Path contractPath = Path.of(contract.getPath());
            if (!Files.exists(contractPath)) {
                ctx.status(404).json("{\"msg\":\"Contract file not found on server\"}");
                return;
            }

            ctx.contentType("application/pdf");
            ctx.header("Content-Disposition", "attachment; filename=\"" + contract.getFilename() + "\"");
            ctx.result(Files.newInputStream(contractPath));
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\":\"Invalid contract ID\"}");
        } catch (Exception e) {
            logger.error("downloadContract failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void deleteContract(Context ctx) {
        try {
            Long contractId = Long.parseLong(ctx.pathParam("contractId"));
            StaffJournalContract contract = contractDAO.read(contractId);
            if (contract != null) {
                try {
                    Files.deleteIfExists(Path.of(contract.getPath()));
                } catch (Exception ignore) {
                    logger.warn("Could not delete contract file from disk: {}", contract.getPath());
                }
                contractDAO.delete(contractId);
            }
            ctx.status(204);
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\":\"Invalid contract ID\"}");
        } catch (Exception e) {
            logger.error("deleteContract failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void getEntries(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("id"));
            List<StaffJournalEntry> entries = entryDAO.readAllByJournalId(journalId);
            ctx.json(entries.stream().map(this::toEntryDTO).collect(Collectors.toList()));
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\":\"Invalid journal ID\"}");
        } catch (Exception e) {
            logger.error("getEntries failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void createEntry(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("id"));
            StaffJournal journal = staffJournalDAO.read(journalId);
            if (journal == null) {
                ctx.status(404).json("{\"msg\":\"Staff journal not found\"}");
                return;
            }

            var tokenUser = ctx.attribute("user");
            User author = null;
            if (tokenUser instanceof JwtUserDTO jwtUser) {
                author = userDAO.readByEmail(jwtUser.getUsername());
            }

            CreateStaffJournalEntryRequestDTO req = ctx.bodyAsClass(CreateStaffJournalEntryRequestDTO.class);
            if (req.getTitle() == null || req.getTitle().isBlank()) {
                ctx.status(400).json("{\"msg\":\"Title is required\"}");
                return;
            }

            StaffJournalEntry entry = new StaffJournalEntry();
            entry.setStaffJournal(journal);
            entry.setTitle(req.getTitle());
            entry.setContent(req.getContent());
            entry.setAuthor(author);

            StaffJournalEntry created = entryDAO.create(entry);
            ctx.status(201).json(toEntryDTO(created));
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\":\"Invalid journal ID\"}");
        } catch (Exception e) {
            logger.error("createEntry failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void updateEntry(Context ctx) {
        try {
            Long entryId = Long.parseLong(ctx.pathParam("entryId"));
            UpdateStaffJournalEntryRequestDTO req = ctx.bodyAsClass(UpdateStaffJournalEntryRequestDTO.class);

            StaffJournalEntry patch = new StaffJournalEntry();
            patch.setTitle(req.getTitle());
            patch.setContent(req.getContent());

            StaffJournalEntry updated = entryDAO.update(entryId, patch);
            if (updated == null) {
                ctx.status(404).json("{\"msg\":\"Entry not found\"}");
                return;
            }
            ctx.json(toEntryDTO(updated));
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\":\"Invalid entry ID\"}");
        } catch (Exception e) {
            logger.error("updateEntry failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void deleteEntry(Context ctx) {
        try {
            Long entryId = Long.parseLong(ctx.pathParam("entryId"));
            entryDAO.delete(entryId);
            ctx.status(204);
        } catch (NumberFormatException e) {
            ctx.status(400).json("{\"msg\":\"Invalid entry ID\"}");
        } catch (Exception e) {
            logger.error("deleteEntry failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    private StaffJournalResponseDTO toResponseDTO(StaffJournal journal, List<StaffJournalEntry> entries, List<StaffJournalContract> contracts) {
        return StaffJournalResponseDTO.builder()
            .id(journal.getId())
            .userId(journal.getUser() != null ? journal.getUser().getId() : null)
            .contracts(contracts.stream().map(this::toContractDTO).collect(Collectors.toList()))
            .entries(entries.stream().map(this::toEntryDTO).collect(Collectors.toList()))
            .build();
    }

    private StaffJournalContractDTO toContractDTO(StaffJournalContract contract) {
        return StaffJournalContractDTO.builder()
            .id(contract.getId())
            .filename(contract.getFilename())
            .uploadedAt(contract.getUploadedAt())
            .build();
    }

    private StaffJournalEntryResponseDTO toEntryDTO(StaffJournalEntry entry) {
        return StaffJournalEntryResponseDTO.builder()
            .id(entry.getId())
            .title(entry.getTitle())
            .content(entry.getContent())
            .authorId(entry.getAuthor() != null ? entry.getAuthor().getId() : null)
            .authorName(entry.getAuthor() != null ? entry.getAuthor().getName() : null)
            .createdAt(entry.getCreatedAt())
            .updatedAt(entry.getUpdatedAt())
            .build();
    }
}
