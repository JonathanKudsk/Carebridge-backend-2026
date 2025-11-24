package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ResidentDAO implements IDAO<Resident, Long> {

    private static final Logger logger = LoggerFactory.getLogger(ResidentDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static ResidentDAO instance;

    public static synchronized ResidentDAO getInstance() {
        if (instance == null) instance = new ResidentDAO();
        return instance;
    }

    public Resident create(Resident resident) {
        if (resident == null) throw new ApiRuntimeException(400, "Resident cannot be null");
        if (resident.getFirstName() == null || resident.getFirstName().isBlank())
            throw new ApiRuntimeException(400, "firstName is required");
        if (resident.getLastName() == null || resident.getLastName().isBlank())
            throw new ApiRuntimeException(400, "lastName is required");
        if (resident.getCprNr() == null || resident.getCprNr().isBlank())
            throw new ApiRuntimeException(400, "cprNr is required");

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(resident);
            em.getTransaction().commit();
            return resident;
        } catch (Exception e) {
            logger.error("Error persisting resident to db", e);
            throw new ApiRuntimeException(500, "Error persisting resident to db: " + e.getMessage());
        }
    }

    public Resident read(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Resident resident = em.find(Resident.class, id);
            if (resident == null) {
                throw new EntityNotFoundException("Resident not found with ID: " + id);
            }
            return resident;
        } catch (Exception e) {
            logger.error("Error retrieving resident from db", e);
            throw new RuntimeException("Error retrieving resident from db.", e);
        }
    }

    public List<Resident> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            List<Resident> residents = em.createQuery("SELECT r FROM Resident r", Resident.class).getResultList();
            return residents;
        } catch (Exception e) {
            logger.error("Error retrieving residents from db", e);
            throw new RuntimeException("Error retrieving residents from db.", e);
        }
    }

    @Override
    public Resident update(Long id, Resident updated) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Resident managed = em.find(Resident.class, id);
            if (managed == null) {
                em.getTransaction().rollback();
                throw new ApiRuntimeException(404, "Resident not found with ID: " + id);
            }
            // copy updatable fields (validate if needed)
            if (updated.getFirstName() != null && !updated.getFirstName().isBlank())
                managed.setFirstName(updated.getFirstName());
            if (updated.getLastName() != null && !updated.getLastName().isBlank())
                managed.setLastName(updated.getLastName());
            if (updated.getCprNr() != null && !updated.getCprNr().isBlank())
                managed.setCprNr(updated.getCprNr());

            em.merge(managed);
            em.getTransaction().commit();
            return managed;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating resident", e);
            throw new ApiRuntimeException(500, "Error updating resident: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Resident managed = em.find(Resident.class, id);
            if (managed == null) {
                em.getTransaction().rollback();
                throw new EntityNotFoundException("Resident not found with ID: " + id);
            }
            em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error deleting resident", e);
            throw new RuntimeException("Error deleting resident.", e);
        }
    }
}