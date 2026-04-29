package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.*;
import com.carebridge.entities.JournalEntry;
import com.carebridge.entities.Journal;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.annotations.DynamicDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/journals/{journalId}/journal-entries")
public class JournalEntryController {

    private final JournalEntryDAO journalEntryDAO;
    private final JournalDAO journalDAO;
    private final UserDAO userDAO;
    private final MappingService mappingService;

    public JournalEntryController(JournalEntryDAO journalEntryDAO, JournalDAO journalDAO, UserDAO userDAO, MappingService mappingService) {
        this.journalEntryDAO = journalEntryDAO;
        this.journalDAO = journalDAO;
        this.userDAO = userDAO;
        this.mappingService = mappingService;
    }

    @GetMapping
    @DynamicDTO
    public List<Long> getAll(@PathVariable Long journalId) {
        return journalEntryDAO.getEntryIdsByJournalId(journalId);
    }

    @GetMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<JournalEntry> getById(@PathVariable Long journalId, @PathVariable Long id) {
        JournalEntry entry = journalEntryDAO.read(id);
        if (entry == null) return ResponseEntity.notFound().build();
        
        if (entry.getJournal() == null || !entry.getJournal().getId().equals(journalId)) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(entry);
    }

    @PostMapping
    @DynamicDTO
    public ResponseEntity<JournalEntry> create(
            @PathVariable Long journalId,
            @RequestBody Map<String, Object> body,
            @RequestAttribute(value = "user", required = false) Map<String, Object> jwtUser) {

        Journal journal = journalDAO.read(journalId);
        if (journal == null) return ResponseEntity.notFound().build();
        
        JournalEntry entry = mappingService.toEntity(body, JournalEntry.class);
        if (entry.getTitle() == null || entry.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        entry.setJournal(journal);

        if (jwtUser != null) {
            String email = (String) jwtUser.get("username");
            User user = userDAO.readByEmail(email);
            if (user != null) {
                entry.setAuthor(user);
            }
        }

        JournalEntry created = journalEntryDAO.create(entry);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<JournalEntry> update(
            @PathVariable Long journalId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        JournalEntry entry = journalEntryDAO.read(id);
        if (entry == null) return ResponseEntity.notFound().build();

        if (entry.getJournal() == null || !entry.getJournal().getId().equals(journalId)) {
            return ResponseEntity.badRequest().build();
        }

        JournalEntry patch = mappingService.toEntity(body, JournalEntry.class);
        JournalEntry updated = journalEntryDAO.update(id, patch);
        return ResponseEntity.ok(updated);
    }
}
