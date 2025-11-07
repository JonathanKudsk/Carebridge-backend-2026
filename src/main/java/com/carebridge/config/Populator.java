package com.carebridge.config;

import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class Populator {
    private static final Logger logger = LoggerFactory.getLogger(Populator.class);

    public static void populate(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

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

            EventType meeting = findEventTypeByName(em, "Meeting");
            if (meeting == null) {
                meeting = new EventType("Meeting", "#007bff");
                em.persist(meeting);
            }

            EventType deadline = findEventTypeByName(em, "Deadline");
            if (deadline == null) {
                deadline = new EventType("Deadline", "#ff9800");
                em.persist(deadline);
            }

            Long eventCount = em.createQuery("SELECT COUNT(e) FROM Event e", Long.class).getSingleResult();
            if (eventCount == 0) {
                Event e1 = new Event(
                        "Weekly Sync",
                        "Discuss goals and blockers.",
                        Instant.now().plusSeconds(86400),
                        true,
                        admin,
                        meeting
                );
                em.persist(e1);

                Event e2 = new Event(
                        "Project Deadline",
                        "Finish MVP and testing.",
                        Instant.now().plusSeconds(172800),
                        true,
                        alice,
                        deadline
                );
                em.persist(e2);
            }

            tx.commit();
            logger.info("Database populated (or already had data).");
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
