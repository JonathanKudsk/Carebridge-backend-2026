package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.StaffJournalEntry;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StaffJournalEntryDAO {

    private static final Logger logger = LoggerFactory.getLogger(StaffJournalEntryDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static StaffJournalEntryDAO instance;

    private StaffJournalEntryDAO() {}

    public static synchronized StaffJournalEntryDAO getInstance() {
        if (instance == null) instance = new StaffJournalEntryDAO();
        return instance;
    }

    public StaffJournalEntry create(StaffJournalEntry entry) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(entry);
            em.getTransaction().commit();
            return entry;
        } catch (Exception e) {
            logger.error("Error creating StaffJournalEntry", e);
            throw new RuntimeException("Error creating StaffJournalEntry", e);
        }
    }

    public StaffJournalEntry read(Long id) {
        try (var em = emf.createEntityManager()) {
            return em.find(StaffJournalEntry.class, id);
        } catch (Exception e) {
            logger.error("Error reading StaffJournalEntry {}", id, e);
            throw new RuntimeException("Error reading StaffJournalEntry", e);
        }
    }

    public List<StaffJournalEntry> readAllByJournalId(Long journalId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                "SELECT e FROM StaffJournalEntry e LEFT JOIN FETCH e.author WHERE e.staffJournal.id = :journalId ORDER BY e.createdAt DESC",
                StaffJournalEntry.class
            ).setParameter("journalId", journalId).getResultList();
        } catch (Exception e) {
            logger.error("Error reading entries for StaffJournal {}", journalId, e);
            throw new RuntimeException("Error reading StaffJournalEntries", e);
        }
    }

    public StaffJournalEntry update(Long id, StaffJournalEntry patch) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            StaffJournalEntry existing = em.find(StaffJournalEntry.class, id);
            if (existing == null) {
                em.getTransaction().rollback();
                return null;
            }
            if (patch.getTitle() != null) existing.setTitle(patch.getTitle());
            if (patch.getContent() != null) existing.setContent(patch.getContent());
            em.getTransaction().commit();
            return em.createQuery(
                "SELECT e FROM StaffJournalEntry e LEFT JOIN FETCH e.author WHERE e.id = :id",
                StaffJournalEntry.class
            ).setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            logger.error("Error updating StaffJournalEntry {}", id, e);
            throw new RuntimeException("Error updating StaffJournalEntry", e);
        }
    }

    public void delete(Long id) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            StaffJournalEntry entry = em.find(StaffJournalEntry.class, id);
            if (entry != null) em.remove(entry);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error deleting StaffJournalEntry {}", id, e);
            throw new RuntimeException("Error deleting StaffJournalEntry", e);
        }
    }
}
