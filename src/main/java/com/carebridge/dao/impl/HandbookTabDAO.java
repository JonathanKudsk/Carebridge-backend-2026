package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.HandbookTab;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandbookTabDAO {
    private static final Logger logger = LoggerFactory.getLogger(HandbookTabDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    private static HandbookTabDAO instance;

    public static synchronized HandbookTabDAO getInstance() {
        if (instance == null) {
            instance = new HandbookTabDAO();
        }
        return instance;
    }

    public HandbookTab create(HandbookTab tab) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(tab);
            em.getTransaction().commit();
            return tab;
        } catch (Exception e) {
            logger.error("Error creating handbook tab", e);
            throw new RuntimeException("Error creating handbook tab", e);
        }
    }

    public HandbookTab findById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            HandbookTab tab = em.find(HandbookTab.class, id);
            if (tab == null) {
                throw new EntityNotFoundException("HandbookTab not found with ID: " + id);
            }
            return tab;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving handbook tab", e);
            throw new RuntimeException("Error retrieving handbook tab", e);
        }
    }

    public HandbookTab update(HandbookTab tab) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            HandbookTab updated = em.merge(tab);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            logger.error("Error updating handbook tab", e);
            throw new RuntimeException("Error updating handbook tab", e);
        }
    }

    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            HandbookTab tab = em.find(HandbookTab.class, id);
            if (tab == null) {
                throw new EntityNotFoundException("HandbookTab not found with ID: " + id);
            }
            em.remove(tab);
            em.getTransaction().commit();
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting handbook tab", e);
            throw new RuntimeException("Error deleting handbook tab", e);
        }
    }

    public int countByHandbookId(Long handbookId) {
        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery("""
                    SELECT COUNT(t)
                    FROM HandbookTab t
                    WHERE t.handbook.id = :handbookId
                    """, Long.class)
                    .setParameter("handbookId", handbookId)
                    .getSingleResult();

            return count.intValue();
        } catch (Exception e) {
            logger.error("Error counting handbook tabs", e);
            throw new RuntimeException("Error counting handbook tabs", e);
        }
    }
}