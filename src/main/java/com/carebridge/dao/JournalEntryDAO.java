package com.carebridge.dao;

import com.carebridge.models.JournalEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class JournalEntryDAOImpl implements IJournalEntryDAO {

    private final EntityManager em;

    public JournalEntryDAOImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public JournalEntry save(JournalEntry entry) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entry);
            // after persist, entry.getId() should be set (because of GenerationType.IDENTITY)
            tx.commit();
            return entry;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e; // bubble up so caller knows it failed
        }
    }

    @Override
    public JournalEntry findById(Long id) {
        return em.find(JournalEntry.class, id);
    }
}

