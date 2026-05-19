package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.StaffJournal;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaffJournalDAO {

    private static final Logger logger = LoggerFactory.getLogger(StaffJournalDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static StaffJournalDAO instance;

    private StaffJournalDAO() {}

    public static synchronized StaffJournalDAO getInstance() {
        if (instance == null) instance = new StaffJournalDAO();
        return instance;
    }

    public StaffJournal create(StaffJournal journal) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(journal);
            em.getTransaction().commit();
            return journal;
        } catch (Exception e) {
            logger.error("Error persisting StaffJournal", e);
            throw new RuntimeException("Error persisting StaffJournal", e);
        }
    }

    public StaffJournal read(Long id) {
        try (var em = emf.createEntityManager()) {
            return em.find(StaffJournal.class, id);
        } catch (Exception e) {
            logger.error("Error reading StaffJournal {}", id, e);
            throw new RuntimeException("Error reading StaffJournal", e);
        }
    }

    public StaffJournal readByUserId(Long userId) {
        try (var em = emf.createEntityManager()) {
            var results = em.createQuery(
                "SELECT s FROM StaffJournal s WHERE s.user.id = :userId",
                StaffJournal.class
            ).setParameter("userId", userId).getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.error("Error reading StaffJournal for user {}", userId, e);
            throw new RuntimeException("Error reading StaffJournal by userId", e);
        }
    }

}
