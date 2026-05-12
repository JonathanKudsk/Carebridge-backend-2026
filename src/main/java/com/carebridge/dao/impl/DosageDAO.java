package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.Dosage;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DosageDAO {

    private static final Logger logger = LoggerFactory.getLogger(DosageDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static DosageDAO instance;

    public static synchronized DosageDAO getInstance() {
        if (instance == null) instance = new DosageDAO();
        return instance;
    }

    public Dosage create(Dosage dosage) {
        if (dosage == null) throw new ApiRuntimeException(400, "Dosage cannot be null");
        if (dosage.getMedicineName() == null || dosage.getMedicineName().isBlank())
            throw new ApiRuntimeException(400, "medicineName is required");
        if (dosage.getDosage() == null || dosage.getDosage().isBlank())
            throw new ApiRuntimeException(400, "dosage is required");
        if (dosage.getFrequency() == null || dosage.getFrequency().isBlank())
            throw new ApiRuntimeException(400, "frequency is required");
        if (dosage.getStartDate() == null)
            throw new ApiRuntimeException(400, "startDate is required");

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(dosage);
            em.getTransaction().commit();
            return dosage;
        } catch (Exception e) {
            logger.error("Error persisting dosage to db", e);
            throw new ApiRuntimeException(500, "Error persisting dosage to db: " + e.getMessage());
        }
    }

    public Dosage update(Long id, Dosage updated) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Dosage managed = em.find(Dosage.class, id);
            if (managed == null) {
                em.getTransaction().rollback();
                throw new ApiRuntimeException(404, "Dosage not found with ID: " + id);
            }

            if (updated.getMedicineName() != null && !updated.getMedicineName().isBlank())
                managed.setMedicineName(updated.getMedicineName());
            if (updated.getDosage() != null && !updated.getDosage().isBlank())
                managed.setDosage(updated.getDosage());
            if (updated.getFrequency() != null && !updated.getFrequency().isBlank())
                managed.setFrequency(updated.getFrequency());
            if (updated.getStartDate() != null)
                managed.setStartDate(updated.getStartDate());
            if (updated.getEndDate() != null)
                managed.setEndDate(updated.getEndDate());
            if (updated.getNote() != null)
                managed.setNote(updated.getNote());

            // opdater timestamp og doctor ID
            managed.setUpdatedAt(updated.getUpdatedAt());
            managed.setUpdatedByUserId(updated.getUpdatedByUserId());

            em.merge(managed);
            em.getTransaction().commit();
            return managed;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating dosage", e);
            throw new ApiRuntimeException(500, "Error updating dosage: " + e.getMessage());
        }
    }

    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Dosage managed = em.find(Dosage.class, id);
            if (managed == null) {
                em.getTransaction().rollback();
                throw new ApiRuntimeException(404, "Dosage not found with ID: " + id);
            }
            em.remove(managed);
            em.getTransaction().commit();
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting dosage", e);
            throw new ApiRuntimeException(500, "Error deleting dosage: " + e.getMessage());
        }
    }

    public Dosage read(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Dosage dosage = em.find(Dosage.class, id);
            if (dosage == null)
                throw new ApiRuntimeException(404, "Dosage not found with ID: " + id);
            return dosage;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving dosage from db", e);
            throw new ApiRuntimeException(500, "Error retrieving dosage from db: " + e.getMessage());
        }
    }

    public List<Dosage> readAllByResident(Long residentId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT d FROM Dosage d WHERE d.resident.id = :residentId", Dosage.class)
                    .setParameter("residentId", residentId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error retrieving dosages from db", e);
            throw new ApiRuntimeException(500, "Error retrieving dosages from db: " + e.getMessage());
        }
    }
}