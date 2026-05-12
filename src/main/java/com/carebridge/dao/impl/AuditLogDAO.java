package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.AuditLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuditLogDAO {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static AuditLogDAO instance;

    public static synchronized AuditLogDAO getInstance() {
        if (instance == null) instance = new AuditLogDAO();
        return instance;
    }

    public AuditLog create(AuditLog log) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(log);
            em.getTransaction().commit();
            return log;
        } catch (Exception e) {
            logger.error("Error persisting AuditLog", e);
            throw new RuntimeException("Error persisting AuditLog", e);
        }
    }

    public List<AuditLog> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading all AuditLogs", e);
            throw new RuntimeException("Error reading all AuditLogs", e);
        }
    }

    public List<AuditLog> readByMedicationId(Long medicationId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT a FROM AuditLog a WHERE a.medicationId = :medId ORDER BY a.timestamp DESC",
                    AuditLog.class)
                    .setParameter("medId", medicationId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading AuditLogs for medication {}", medicationId, e);
            throw new RuntimeException("Error reading AuditLogs for medication " + medicationId, e);
        }
    }
}
