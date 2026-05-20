package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.StaffJournalContract;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StaffJournalContractDAO {

    private static final Logger logger = LoggerFactory.getLogger(StaffJournalContractDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static StaffJournalContractDAO instance;

    private StaffJournalContractDAO() {}

    public static synchronized StaffJournalContractDAO getInstance() {
        if (instance == null) instance = new StaffJournalContractDAO();
        return instance;
    }

    public StaffJournalContract create(StaffJournalContract contract) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(contract);
            em.getTransaction().commit();
            return contract;
        } catch (Exception e) {
            logger.error("Error creating StaffJournalContract", e);
            throw new RuntimeException("Error creating StaffJournalContract", e);
        }
    }

    public StaffJournalContract read(Long id) {
        try (var em = emf.createEntityManager()) {
            return em.find(StaffJournalContract.class, id);
        } catch (Exception e) {
            logger.error("Error reading StaffJournalContract {}", id, e);
            throw new RuntimeException("Error reading StaffJournalContract", e);
        }
    }

    public List<StaffJournalContract> readAllByJournalId(Long journalId) {
        try (var em = emf.createEntityManager()) {
            return em.createQuery(
                "SELECT c FROM StaffJournalContract c WHERE c.staffJournal.id = :journalId ORDER BY c.uploadedAt DESC",
                StaffJournalContract.class
            ).setParameter("journalId", journalId).getResultList();
        } catch (Exception e) {
            logger.error("Error reading contracts for StaffJournal {}", journalId, e);
            throw new RuntimeException("Error reading StaffJournalContracts", e);
        }
    }

    public void delete(Long id) {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            StaffJournalContract contract = em.find(StaffJournalContract.class, id);
            if (contract != null) em.remove(contract);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error deleting StaffJournalContract {}", id, e);
            throw new RuntimeException("Error deleting StaffJournalContract", e);
        }
    }
}
