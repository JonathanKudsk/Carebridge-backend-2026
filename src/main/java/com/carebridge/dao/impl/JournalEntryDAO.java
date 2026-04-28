package com.carebridge.dao.impl;

import com.carebridge.dao.IDAO;
import com.carebridge.entities.JournalEntry;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class JournalEntryDAO implements IDAO<JournalEntry, Long> {

    @PersistenceContext
    private EntityManager em;

    public JournalEntryDAO() {
    }

    @Override
    @Transactional
    public JournalEntry create(JournalEntry entry) {
        em.persist(entry);
        return entry;
    }

    @Override
    public JournalEntry read(Long id) {
        return em.find(JournalEntry.class, id);
    }

    @Override
    public List<JournalEntry> readAll() {
        return em.createQuery("FROM JournalEntry", JournalEntry.class).getResultList();
    }

    @Override
    @Transactional
    public JournalEntry update(Long id, JournalEntry updated) {
        JournalEntry existing = em.find(JournalEntry.class, id);
        if (existing == null) throw new ApiRuntimeException(404, "Entry not found");
        if (updated.getContent() != null) existing.setContent(updated.getContent());
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        JournalEntry entry = em.find(JournalEntry.class, id);
        if (entry != null) em.remove(entry);
    }

    public List<Long> getEntryIdsByJournalId(Long journalId) {
        return em.createQuery("SELECT e.id FROM JournalEntry e WHERE e.journal.id = :jid", Long.class)
                .setParameter("jid", journalId)
                .getResultList();
    }
}
