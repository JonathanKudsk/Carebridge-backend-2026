package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.EventType;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EventTypeDAO implements IDAO<EventType, Long> {

    private static final Logger logger = LoggerFactory.getLogger(EventTypeDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static EventTypeDAO instance;

    private EventTypeDAO() {
    }

    public static synchronized EventTypeDAO getInstance() {
        if (instance == null) instance = new EventTypeDAO();
        return instance;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    @Override
    public EventType read(Long id) {
        try (var em = em()) {
            return em.find(EventType.class, id);
        } catch (Exception e) {
            logger.error("Error reading EventType id={}", id, e);
            throw new ApiRuntimeException(500, "Error reading event type: " + e.getMessage());
        }
    }

    public EventType readByName(String name) {
        try (var em = em()) {
            if (name == null || name.isBlank())
                throw new ValidationException("EventType name cannot be blank");

            var list = em.createQuery("SELECT e FROM EventType e WHERE e.name = :name", EventType.class)
                    .setParameter("name", name)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (ValidationException e) {
            throw new ApiRuntimeException(400, e.getMessage());
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            logger.error("Error fetching EventType by name={}", name, e);
            throw new ApiRuntimeException(500, "Error fetching event type by name: " + e.getMessage());
        }
    }

    @Override
    public List<EventType> readAll() {
        try (var em = em()) {
            return em.createQuery("SELECT e FROM EventType e ORDER BY e.name", EventType.class)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading all EventTypes", e);
            throw new ApiRuntimeException(500, "Error reading all event types: " + e.getMessage());
        }
    }

    @Override
    public EventType create(EventType e) {
        if (e == null) throw new ApiRuntimeException(400, "EventType cannot be null");
        if (e.getName() == null || e.getName().isBlank())
            throw new ApiRuntimeException(400, "EventType name is required");

        try (var em = em()) {
            em.getTransaction().begin();

            boolean exists = !em.createQuery("SELECT et FROM EventType et WHERE et.name = :name", EventType.class)
                    .setParameter("name", e.getName())
                    .getResultList()
                    .isEmpty();
            if (exists)
                throw new ApiRuntimeException(409, "EventType already exists");

            em.persist(e);
            em.getTransaction().commit();
            logger.info("EventType created: {}", e.getName());
            return e;
        } catch (ApiRuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error creating EventType name={}", e.getName(), ex);
            throw new ApiRuntimeException(500, "Error creating event type: " + ex.getMessage());
        }
    }

    @Override
    public EventType update(Long id, EventType updated) {
        try (var em = em()) {
            em.getTransaction().begin();
            EventType existing = em.find(EventType.class, id);
            if (existing == null)
                throw new ApiRuntimeException(404, "EventType not found");

            if (updated.getName() != null && !updated.getName().isBlank())
                existing.setName(updated.getName());
            if (updated.getColorHex() != null)
                existing.setColorHex(updated.getColorHex());

            em.getTransaction().commit();
            logger.info("EventType updated: id={}", id);
            return existing;
        } catch (ApiRuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error updating EventType id={}", id, ex);
            throw new ApiRuntimeException(500, "Error updating event type: " + ex.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            em.getTransaction().begin();
            EventType e = em.find(EventType.class, id);
            if (e == null)
                throw new ApiRuntimeException(404, "EventType not found");
            em.remove(e);
            em.getTransaction().commit();
            logger.info("EventType deleted: id={}", id);
        } catch (ApiRuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error deleting EventType id={}", id, ex);
            throw new ApiRuntimeException(500, "Error deleting event type: " + ex.getMessage());
        }
    }
}
