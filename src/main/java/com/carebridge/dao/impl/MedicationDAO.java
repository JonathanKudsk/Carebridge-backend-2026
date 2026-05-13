package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Medication;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MedicationDAO implements IDAO<Medication, Long> {

    private static final Logger logger = LoggerFactory.getLogger(MedicationDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static MedicationDAO instance;

    public static synchronized MedicationDAO getInstance() {
        if (instance == null) instance = new MedicationDAO();
        return instance;
    }

    @Override
    public Medication create(Medication medication) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(medication);
            em.getTransaction().commit();
            return medication;
        } catch (Exception e) {
            logger.error("Error persisting Medication", e);
            throw new RuntimeException("Error persisting Medication", e);
        }
    }

    @Override
    public Medication read(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            // JOIN FETCH to avoid LazyInitializationException when checking chart ownership on detached entity
            return em.createQuery(
                    "SELECT m FROM Medication m JOIN FETCH m.medicationChart WHERE m.id = :id",
                    Medication.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new RuntimeException("Medication not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error reading Medication", e);
            throw new RuntimeException("Error reading Medication", e);
        }
    }

    @Override
    public List<Medication> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT m FROM Medication m", Medication.class).getResultList();
        } catch (Exception e) {
            logger.error("Error reading all Medications", e);
            throw new RuntimeException("Error reading all Medications", e);
        }
    }

    @Override
    public Medication update(Long id, Medication updated) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Medication existing = em.find(Medication.class, id);
            if (existing == null) throw new RuntimeException("Medication not found with ID: " + id);
            existing.setName(updated.getName());
            existing.setDosage(updated.getDosage());
            existing.setFrequency(updated.getFrequency());
            existing.setStartDate(updated.getStartDate());
            existing.setEndDate(updated.getEndDate());
            existing.setPrescribingDoctor(updated.getPrescribingDoctor());
            existing.setNotes(updated.getNotes());
            existing.setActive(updated.isActive());
            em.getTransaction().commit();
            return existing;
        } catch (Exception e) {
            logger.error("Error updating Medication", e);
            throw new RuntimeException("Error updating Medication", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Medication medication = em.find(Medication.class, id);
            if (medication == null) throw new RuntimeException("Medication not found with ID: " + id);
            em.remove(medication);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error deleting Medication", e);
            throw new RuntimeException("Error deleting Medication", e);
        }
    }
}
