package com.carebridge.services;


import com.carebridge.dao.JournalEntryDAO;
import com.carebridge.dao.JournalDAO;
import com.carebridge.dao.UserDAO;
import com.carebridge.dtos.CreateJournalEntryRequestDTO;
import com.carebridge.dtos.JournalEntryResponseDTO;
import com.carebridge.models.Journal;
import com.carebridge.models.JournalEntry;
import com.carebridge.models.User;

import java.time.LocalDateTime;

public class JournalEntryService {

    private final JournalEntryDAO journalEntryDAO;
    private final JournalDAO journalDAO;
    private final UserDAO userDAO;

    public JournalEntryService(JournalEntryDAO journalEntryDAO,
                               JournalDAO journalDAO,
                               UserDAO userDAO) {
        this.journalEntryDAO = journalEntryDAO;
        this.journalDAO = journalDAO;
        this.userDAO = userDAO;
    }

    /**
     * Creates a new journal entry for a journal.
     * Fulfills: timestamps, author info, persistence.
     */
    public JournalEntryResponseDTO createJournalEntry(CreateJournalEntryRequestDTO requestDTO) {

        // --- 1. Fetch Journal and Author ---
        Journal journal = journalDAO.findById(requestDTO.getJournalId());
        if (journal == null) {
            throw new IllegalArgumentException("Journal not found with ID: " + requestDTO.getJournalId());
        }

        User author = userDAO.findById(requestDTO.getAuthorUserId());
        if (author == null) {
            throw new IllegalArgumentException("Author not found with ID: " + requestDTO.getAuthorUserId());
        }

        // --- 2. Validate Required Input ---
        if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (requestDTO.getContent() == null || requestDTO.getContent().isBlank()) {
            throw new IllegalArgumentException("Content is required.");
        }
        if (requestDTO.getRiskAssessment() == null) {
            throw new IllegalArgumentException("Risk assessment is required.");
        }

        // --- 3. Build new JournalEntry entity ---
        JournalEntry entry = new JournalEntry();
        entry.setJournal(journal);
        entry.setAuthor(author);
        entry.setTitle(requestDTO.getTitle());
        entry.setContent(requestDTO.getContent());
        entry.setRiskAssessment(requestDTO.getRiskAssessment());

        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);

        // --- 4. Save entry via DAO (handles transaction internally) ---
        journalEntryDAO.save(entry);

        // --- 5. Build and return Response DTO ---
        return new JournalEntryResponseDTO(
                entry.getId(),
                journal.getId(),
                author.getId(),
                entry.getTitle(),
                entry.getContent(),
                entry.getRiskAssessment(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}