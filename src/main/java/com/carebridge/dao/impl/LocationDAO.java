package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Location;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LocationDAO implements IDAO<Location,Long> {

    private static final Logger logger = LoggerFactory.getLogger(LocationDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static LocationDAO instance;

    private LocationDAO() {
    }

    public static synchronized LocationDAO getInstance() {
        if (instance == null) instance = new LocationDAO();
        return instance;
    }

    @Override
    public Location read(Long id) {
        try (var em = em()) {
            return em.createQuery("SELECT l FROM Location l where l.id = :id", Location.class).setParameter("id",id)
                    .getSingleResult();
        } catch (NoResultException e) { //if nothing exists in DB return nothing
            return null;
        }
        catch (Exception e) {
            logger.error("Error finding Location", e);
            throw new ApiRuntimeException(500, "Error finding Location: " + e.getMessage());
        }
    }

    @Override
    public List<Location> readAll() {
        try (var em = em()) {
            return em.createQuery("SELECT l FROM Location l ORDER BY l.id", Location.class)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading all Templates", e);
            throw new ApiRuntimeException(500, "Error reading all Templates: " + e.getMessage());
        }
    }

    @Override
    public Location create(Location template) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(template);
            em.getTransaction().commit();
            return template;
        } catch (Exception e) {
            logger.error("Error creating Location", e);
            throw new ApiRuntimeException(500, "Error creating Location: " + e.getMessage());
        }
    }

    @Override
    public Location update(Long id, Location location) {
        try (var em = em()) {
            EntityTransaction transaction = em.getTransaction();
            try {
                location.setId(id);
                transaction.begin();
                em.merge(location);
                transaction.commit();
                return location;
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                logger.error("Error deleting Location", e);
                throw new ApiRuntimeException(500, "Error Deleting Location: " + e.getMessage());
            }
        }
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            EntityTransaction transaction = em.getTransaction();
            try {
                transaction.begin();
                Location object = em.find(Location.class,id);
                em.remove(object);
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                logger.error("Error deleting Location", e);
                throw new ApiRuntimeException(500, "Error Deleting Location: " + e.getMessage());
            }
        }
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }
}