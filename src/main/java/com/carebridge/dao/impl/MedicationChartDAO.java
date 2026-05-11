package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.Medication;
import com.carebridge.entities.MedicationChart;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedicationChartDAO {

    private static final Logger logger = LoggerFactory.getLogger(MedicationChartDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static MedicationChartDAO instance;

    public static synchronized MedicationChartDAO getInstance() {
        if (instance == null) instance = new MedicationChartDAO();
        return instance;
    }

    public MedicationChart create(MedicationChart chart) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(chart);
            em.getTransaction().commit();
            return chart;
        } catch (Exception e) {
            logger.error("Error persisting MedicationChart", e);
            throw new RuntimeException("Error persisting MedicationChart", e);
        }
    }

    public MedicationChart read(Long chartId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT mc FROM MedicationChart mc " +
                    "LEFT JOIN FETCH mc.medications " +
                    "LEFT JOIN FETCH mc.resident " +
                    "WHERE mc.id = :id",
                    MedicationChart.class)
                    .setParameter("id", chartId)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new RuntimeException("MedicationChart not found with ID: " + chartId);
        } catch (Exception e) {
            logger.error("Error reading MedicationChart", e);
            throw new RuntimeException("Error reading MedicationChart", e);
        }
    }

    public MedicationChart readByResidentId(Long residentId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT mc FROM MedicationChart mc " +
                    "LEFT JOIN FETCH mc.medications " +
                    "LEFT JOIN FETCH mc.resident " +
                    "WHERE mc.resident.id = :residentId",
                    MedicationChart.class)
                    .setParameter("residentId", residentId)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new RuntimeException("MedicationChart not found for resident ID: " + residentId);
        } catch (Exception e) {
            logger.error("Error reading MedicationChart by residentId", e);
            throw new RuntimeException("Error reading MedicationChart by residentId", e);
        }
    }

    public Medication addMedication(Long chartId, Medication medication) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            MedicationChart chart = em.find(MedicationChart.class, chartId);
            if (chart == null) throw new RuntimeException("MedicationChart not found with ID: " + chartId);
            medication.setMedicationChart(chart);
            em.persist(medication);
            em.getTransaction().commit();
            return medication;
        } catch (Exception e) {
            logger.error("Error adding medication to chart", e);
            throw new RuntimeException("Error adding medication to chart", e);
        }
    }

    public void removeMedication(Long chartId, Long medicationId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Medication medication = em.createQuery(
                    "SELECT m FROM Medication m JOIN FETCH m.medicationChart WHERE m.id = :id",
                    Medication.class)
                    .setParameter("id", medicationId)
                    .getSingleResult();
            if (!medication.getMedicationChart().getId().equals(chartId)) {
                throw new IllegalArgumentException(
                        "Medication " + medicationId + " does not belong to chart " + chartId);
            }
            em.remove(medication);
            em.getTransaction().commit();
        } catch (NoResultException e) {
            throw new RuntimeException("Medication not found with ID: " + medicationId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error removing medication from chart", e);
            throw new RuntimeException("Error removing medication from chart", e);
        }
    }
}
