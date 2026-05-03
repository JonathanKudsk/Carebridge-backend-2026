package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Event;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.enums.EventAccessLevel;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public class EventDAO implements IDAO<Event, Long> {

    private static final Logger logger = LoggerFactory.getLogger(EventDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static EventDAO instance;

    private EventDAO() {
    }

    public static synchronized EventDAO getInstance() {
        if (instance == null) instance = new EventDAO();
        return instance;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    private Event readWithFetch(EntityManager em, Long id) {
        var list = em.createQuery(
                        "SELECT DISTINCT e FROM Event e " +
                                "LEFT JOIN FETCH e.seenByUsers " +
                                "LEFT JOIN FETCH e.createdBy " +
                                "LEFT JOIN FETCH e.eventType " +
                                "WHERE e.id = :id",
                        Event.class
                )
                .setParameter("id", id)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Event read(Long id) {
        try (var em = em()) {
            return readWithFetch(em, id);
        } catch (Exception e) {
            logger.error("Error reading Event id={}", id, e);
            throw new ApiRuntimeException(500, "Error reading event: " + e.getMessage());
        }
    }

    @Override
    public List<Event> readAll() {
        try (var em = em()) {
            return em.createQuery(
                    "SELECT DISTINCT e FROM Event e " +
                            "LEFT JOIN FETCH e.seenByUsers " +
                            "ORDER BY e.startAt",
                    Event.class
            ).getResultList();
        } catch (Exception e) {
            logger.error("Error reading all Events", e);
            throw new ApiRuntimeException(500, "Error reading all events: " + e.getMessage());
        }
    }

    public List<Event> readByCreator(Long userId) {
        try (var em = em()) {
            if (userId == null)
                throw new ValidationException("User ID cannot be null");

            return em.createQuery("SELECT e FROM Event e WHERE e.createdBy.id = :uid ORDER BY e.startAt", Event.class)
                    .setParameter("uid", userId)
                    .getResultList();
        } catch (ValidationException e) {
            throw new ApiRuntimeException(400, e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching Events by creator userId={}", userId, e);
            throw new ApiRuntimeException(500, "Error fetching events by creator: " + e.getMessage());
        }
    }

    @Override
    public Event create(Event e) {
        if (e == null)
            throw new ApiRuntimeException(400, "Event cannot be null");
        if (e.getTitle() == null || e.getTitle().isBlank())
            throw new ApiRuntimeException(400, "Event title is required");
        if (e.getStartAt() == null)
            throw new ApiRuntimeException(400, "Event startAt is required");
        if (e.getStartAt().isBefore(Instant.now()))
            throw new ApiRuntimeException(400, "Event startAt must be in the future");
        if (e.getCreatedBy() == null)
            throw new ApiRuntimeException(400, "Event createdBy is required");
        if (e.getEventType() == null)
            throw new ApiRuntimeException(400, "Event eventType is required");

        try (var em = em()) {
            em.getTransaction().begin();
            em.persist(e);
            em.getTransaction().commit();
            logger.info("Event created: title='{}', id={}", e.getTitle(), e.getId());
            return readWithFetch(em, e.getId());
        } catch (Exception ex) {
            logger.error("Error creating Event title='{}'", e.getTitle(), ex);
            throw new ApiRuntimeException(500, "Error creating event: " + ex.getMessage());
        }
    }

    @Override
    public Event update(Long id, Event updated) {
        try (var em = em()) {
            em.getTransaction().begin();
            Event existing = em.find(Event.class, id);
            if (existing == null)
                throw new ApiRuntimeException(404, "Event not found");

            if (updated.getTitle() != null && !updated.getTitle().isBlank())
                existing.setTitle(updated.getTitle());
            if (updated.getDescription() != null)
                existing.setDescription(updated.getDescription());
            if (updated.getStartAt() != null)
                existing.setStartAt(updated.getStartAt());
            existing.setShowOnBoard(updated.isShowOnBoard());

            if (updated.getEventType() != null)
                existing.setEventType(updated.getEventType());
            if (updated.getCreatedBy() != null)
                existing.setCreatedBy(updated.getCreatedBy());

            if (updated.getSeenByUsers() != null && !updated.getSeenByUsers().isEmpty()) {
                existing.getSeenByUsers().addAll(updated.getSeenByUsers());
            }

            em.getTransaction().commit();
            return readWithFetch(em, id);
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRuntimeException(500, "Error updating event: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            em.getTransaction().begin();
            Event e = em.find(Event.class, id);
            if (e == null)
                throw new ApiRuntimeException(404, "Event not found");
            em.remove(e);
            em.getTransaction().commit();
            logger.info("Event deleted: id={}", id);
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting Event id={}", id, e);
            throw new ApiRuntimeException(500, "Error deleting event: " + e.getMessage());
        }
    }

    public void addSeenByUser(Long eventId, User user) {
        if (eventId == null || user == null) {
            throw new ApiRuntimeException(400, "Event id and user are required");
        }

        try (var em = em()) {
            em.getTransaction().begin();

            Event event = em.find(Event.class, eventId);
            if (event == null) {
                em.getTransaction().rollback();
                throw new ApiRuntimeException(404, "Event not found");
            }

            User managedUser = em.getReference(User.class, user.getId());
            event.getSeenByUsers().add(managedUser);

            em.getTransaction().commit();
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error marking event as seen. eventId={}, userId={}", eventId, user.getId(), e);
            throw new ApiRuntimeException(500, "Error marking event as seen: " + e.getMessage());
        }
    }

    public void removeSeenByUser(Long eventId, User user) {
        if (eventId == null || user == null) {
            throw new ApiRuntimeException(400, "Event id and user are required");
        }

        try (var em = em()) {
            em.getTransaction().begin();

            Event event = em.find(Event.class, eventId);
            if (event == null) {
                em.getTransaction().rollback();
                throw new ApiRuntimeException(404, "Event not found");
            }

            User managedUser = em.getReference(User.class, user.getId());
            event.getSeenByUsers().remove(managedUser);

            em.getTransaction().commit();
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error unmarking event as seen. eventId={}, userId={}", eventId, user.getId(), e);
            throw new ApiRuntimeException(500, "Error unmarking event as seen: " + e.getMessage());
        }
    }

    private int roleToAccessLevel(Role role) {
        return switch (role) {
            case ADMIN      -> 5;
            case GUARDIAN   -> 4;
            case CAREWORKER -> 3;
            default         -> 1;
        };
    }

    public List<Event> readAccessibleEvents(Long userId) {
        try (var em = em()) {
            User user = em.find(User.class, userId);
            if (user == null) throw new ApiRuntimeException(404, "User not found");
            int userLevel = roleToAccessLevel(user.getRole());

            return em.createQuery(
                            "SELECT DISTINCT e FROM Event e " +
                            "LEFT JOIN FETCH e.usersWithAccess " +
                            "LEFT JOIN FETCH e.createdBy " +
                            "LEFT JOIN FETCH e.eventType " +
                            "WHERE EXISTS (SELECT u FROM e.usersWithAccess u WHERE u.id = :userId) " +
                            "   OR (e.accessLevel = :roleBased AND :userLevel >= e.riskLevel) " +
                            "ORDER BY e.startAt",
                            Event.class)
                    .setParameter("userId", userId)
                    .setParameter("roleBased", EventAccessLevel.ROLE_BASED)
                    .setParameter("userLevel", userLevel)
                    .getResultList();
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error reading accessible events for userId={}", userId, e);
            throw new ApiRuntimeException(500, "Error reading accessible events: " + e.getMessage());
        }
    }

    public List<Event> readAccessibleEventsBetween(Long userId, Instant from, Instant to) {
        try (var em = em()) {
            User user = em.find(User.class, userId);
            if (user == null) throw new ApiRuntimeException(404, "User not found");
            int userLevel = roleToAccessLevel(user.getRole());

            return em.createQuery(
                            "SELECT DISTINCT e FROM Event e " +
                            "LEFT JOIN FETCH e.usersWithAccess " +
                            "LEFT JOIN FETCH e.createdBy " +
                            "LEFT JOIN FETCH e.eventType " +
                            "WHERE e.startAt >= :from AND e.startAt < :to " +
                            "AND (EXISTS (SELECT u FROM e.usersWithAccess u WHERE u.id = :userId) " +
                            "     OR (e.accessLevel = :roleBased AND :userLevel >= e.riskLevel)) " +
                            "ORDER BY e.startAt ASC",
                            Event.class)
                    .setParameter("userId", userId)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .setParameter("roleBased", EventAccessLevel.ROLE_BASED)
                    .setParameter("userLevel", userLevel)
                    .getResultList();
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error reading accessible events between {} and {} for userId={}", from, to, userId, e);
            throw new ApiRuntimeException(500, "Error reading accessible events between dates: " + e.getMessage());
        }
    }

    public Event readAccessibleById(Long eventId, Long userId, Integer userAccessLevel) {
        try (var em = em()) {
            var list = em.createQuery(
                            "SELECT DISTINCT e FROM Event e " +
                            "LEFT JOIN FETCH e.usersWithAccess " +
                            "LEFT JOIN FETCH e.createdBy " +
                            "LEFT JOIN FETCH e.eventType " +
                            "WHERE e.id = :eventId " +
                            "AND (EXISTS (SELECT u FROM e.usersWithAccess u WHERE u.id = :userId) " +
                            "     OR (e.accessLevel = :roleBased AND :userLevel >= e.riskLevel))",
                            Event.class)
                    .setParameter("eventId", eventId)
                    .setParameter("userId", userId)
                    .setParameter("roleBased", EventAccessLevel.ROLE_BASED)
                    .setParameter("userLevel", userAccessLevel)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            logger.error("Error reading accessible event id={} for userId={}", eventId, userId, e);
            throw new ApiRuntimeException(500, "Error reading event: " + e.getMessage());
        }
    }

    public List<Event> readBetween(Instant from, Instant to) {
        try (var em = em()) {
            return em.createQuery(
                            "SELECT DISTINCT e FROM Event e " +
                                    "LEFT JOIN FETCH e.seenByUsers " +
                                    "WHERE e.startAt >= :from AND e.startAt < :to " +
                                    "ORDER BY e.startAt ASC",
                            Event.class
                    )
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading events between {} and {}", from, to, e);
            throw new ApiRuntimeException(500, "Error reading events between dates: " + e.getMessage());
        }
    }
}
