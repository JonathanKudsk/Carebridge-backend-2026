package com.carebridge.config;

import com.carebridge.entities.EventType;
import com.carebridge.entities.Journal;
import com.carebridge.entities.Resident;
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
                alice.setRole(Role.CAREWORKER);
                alice.setDisplayName("Alice User");
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

            // --- RESIDENT DUMMY DATA ---
            Resident resident1 = findResidentByCprNr(em, "010150-1111");
            if (resident1 == null) {
                resident1 = new Resident();
                resident1.setFirstName("Anna");
                resident1.setLastName("Hansen");
                resident1.setCprNr("010150-1111");
                
                Journal journal1 = new Journal();
                journal1.setResident(resident1);
                resident1.setJournal(journal1);
                
                em.persist(resident1);
            }

            Resident resident2 = findResidentByCprNr(em, "050560-2222");
            if (resident2 == null) {
                resident2 = new Resident();
                resident2.setFirstName("Bent");
                resident2.setLastName("Olsen");
                resident2.setCprNr("050560-2222");
                
                Journal journal2 = new Journal();
                journal2.setResident(resident2);
                resident2.setJournal(journal2);
                
                em.persist(resident2);
            }

            Resident resident3 = findResidentByCprNr(em, "101070-3333");
            if (resident3 == null) {
                resident3 = new Resident();
                resident3.setFirstName("Cecilie");
                resident3.setLastName("Nielsen");
                resident3.setCprNr("101070-3333");
                
                Journal journal3 = new Journal();
                journal3.setResident(resident3);
                resident3.setJournal(journal3);
                
                em.persist(resident3);
            }
            // -------------------------------

            tx.commit();
            logger.info("Database populated successfully (users + event types + journal + residents).");
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

    // --- NEW HELPER METHOD ---
    private static Resident findResidentByCprNr(EntityManager em, String cprNr) {
        var list = em.createQuery("SELECT r FROM Resident r WHERE r.cprNr = :cprNr", Resident.class)
                .setParameter("cprNr", cprNr)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}