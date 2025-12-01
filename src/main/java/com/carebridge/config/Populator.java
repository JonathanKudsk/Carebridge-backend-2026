package com.carebridge.config;

import com.carebridge.entities.EventType;
import com.carebridge.entities.Journal;
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

            User admin = findUserByEmail(em, "admin@carebridge.io");
            if (admin == null) {
                admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@carebridge.io");
                admin.setPassword("admin123");
                admin.setRole(Role.ADMIN);
                admin.setDisplayName("Admin User");
                admin.setDisplayEmail("admin@carebridge.io");
                admin.setDisplayPhone("000-0000-0000");
                admin.setInternalEmail("admin.internal@carebridge.io");
                admin.setInternalPhone("111-1111-1111");
                em.persist(admin);
            }

            User alice = findUserByEmail(em, "alice@carebridge.io");
            if (alice == null) {
                alice = new User();
                alice.setName("Alice");
                alice.setEmail("alice@carebridge.io");
                alice.setPassword("password123");
                alice.setRole(Role.USER);
                alice.setDisplayName("Alice Smith");
                alice.setDisplayEmail("alice@carebridge.io");
                alice.setDisplayPhone("222-2222-2222");
                alice.setInternalEmail("alice.internal@carebridge.io");
                alice.setInternalPhone("333-3333-3333");
                em.persist(alice);
            }

            List<EventType> predefinedTypes = List.of(
                    new EventType("Meeting", "#007bff"),
                    new EventType("Task", "#28a745"),
                    new EventType("Reminder", "#ffc107"),
                    new EventType("Holiday", "#dc3545"),
                    new EventType("Private", "#6f42c1"),
                    new EventType("Other", "#adb5bd")
            );

            for (EventType type : predefinedTypes) {
                EventType existing = findEventTypeByName(em, type.getName());
                if (existing == null) {
                    em.persist(type);
                }
            }

            // --- Insert a test journal if none exists ---
            long journalCount = em.createQuery("SELECT COUNT(j) FROM Journal j", Long.class)
                    .getSingleResult();
            if (journalCount == 0) {
                Journal journal = new Journal();
                // If you don't add @GeneratedValue to Journal.id, you MUST set an ID manually:
                // journal.setId(1L);
                em.persist(journal);
                logger.info("Added test Journal with auto-generated ID");
            }

            tx.commit();
            logger.info("Database populated successfully (users + event types + journal).");
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            logger.error("Population failed", ex);
            throw ex;
        } finally {
            em.close();
        }
    }

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
