package com.carebridge.config;

import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Populator {
    private static final Logger logger = LoggerFactory.getLogger(Populator.class);

    public static void populate(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // ---- USERS ----
            User admin = findUserByEmail(em, "admin@carebridge.io");
            if (admin == null) {
                admin = new User("Admin", "admin@carebridge.io", "admin123", Role.ADMIN);
                em.persist(admin);
            }

            User alice = findUserByEmail(em, "alice@carebridge.io");
            if (alice == null) {
                alice = new User("Alice", "alice@carebridge.io", "password123", Role.USER);
                em.persist(alice);
            }

            // ---- EVENT TYPES ----
            List<EventType> predefinedTypes = List.of(
                    new EventType("Meeting",  "#007bff"), // blue
                    new EventType("Task",     "#28a745"), // green
                    new EventType("Reminder", "#ffc107"), // yellow
                    new EventType("Holiday",  "#dc3545"), // red
                    new EventType("Private",  "#6f42c1"), // purple
                    new EventType("Other",    "#adb5bd")  // gray
            );

            for (EventType type : predefinedTypes) {
                EventType existing = findEventTypeByName(em, type.getName());
                if (existing == null) {
                    em.persist(type);
                }
            }

            tx.commit();
            logger.info("Database populated successfully (users + event types).");
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            logger.error("Population failed", ex);
            throw ex;
        } finally {
            em.close();
        }
    }

    // ---- HELPERS ----
    private static User findUserByEmail(EntityManager em, String email) {
        var list = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private static EventType findEventTypeByName(EntityManager em, String name) {
        var list = em.createQuery("SELECT et FROM EventType et WHERE et.name = :name", EventType.class)
                .setParameter("name", name)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}
