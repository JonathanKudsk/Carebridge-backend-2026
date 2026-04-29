package com.carebridge.dao.impl;

import com.carebridge.dao.IDAO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.JournalEntry;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class JournalDAO implements IDAO<Journal, Long> {

    @PersistenceContext
    private EntityManager em;

    public JournalDAO() {
    }

    @Override
    @Transactional
    public Journal create(Journal journal) {
        em.persist(journal);
        return journal;
    }

    @Override
    public Journal read(Long id) {
        return em.find(Journal.class, id);
    }

    @Override
    public List<Journal> readAll() {
        return em.createQuery("FROM Journal", Journal.class).getResultList();
    }

    @Override
    @Transactional
    public Journal update(Long id, Journal updated) {
        Journal managed = em.find(Journal.class, id);
        if (managed == null) throw new ApiRuntimeException(404, "Journal not found");
        return managed;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Journal managed = em.find(Journal.class, id);
        if (managed != null) em.remove(managed);
    }

    @Transactional
    public void addEntryToJournal(Journal journal, JournalEntry entry) {
        Journal managed = em.find(Journal.class, journal.getId());
        JournalEntry managedEntry = em.find(JournalEntry.class, entry.getId());
        managed.addEntry(managedEntry);
    }
}
