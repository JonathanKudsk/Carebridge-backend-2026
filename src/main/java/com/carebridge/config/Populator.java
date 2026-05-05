package com.carebridge.config;

import com.carebridge.entities.EventType;
import com.carebridge.entities.Journal;
import com.carebridge.entities.User;
import com.carebridge.entities.Resident;
import com.carebridge.entities.JournalEntry;
import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;

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

            User guardian = findUserByEmail(em, "guardian@carebridge.io");
            if (guardian == null) {
                guardian = new User();
                guardian.setName("Guardian");
                guardian.setEmail("guardian@carebridge.io");
                guardian.setPassword("guardian123");
                guardian.setRole(Role.GUARDIAN);
                guardian.setDisplayName("Guardian User");
                guardian.setDisplayEmail("guardian@carebridge.io");
                guardian.setDisplayPhone("444-4444-4444");
                em.persist(guardian);
            }

            Resident linkedResident = new Resident();
            linkedResident.setFirstName("Anna");
            linkedResident.setLastName("Andersen");
            linkedResident.setCprNr("010101-1234");

            Journal linkedJournal = new Journal();
            linkedJournal.setEntries(new ArrayList<>());
            linkedJournal.setResident(linkedResident);
            linkedResident.setJournal(linkedJournal);

            guardian.addResident(linkedResident);

            JournalEntry linkedEntry = new JournalEntry();
            linkedEntry.setAuthor(alice);
            linkedEntry.setJournal(linkedJournal);
            linkedEntry.setTitle("Linked resident note");
            linkedEntry.setContent("Guardian should be allowed to see this note.");
            linkedEntry.setEntryType(EntryType.NOTE);
            linkedEntry.setRiskAssessment(RiskAssessment.LOW);
            linkedEntry.setCreatedAt(LocalDateTime.now());
            linkedEntry.setUpdatedAt(LocalDateTime.now());
            linkedEntry.setEditCloseTime(LocalDateTime.now().plusHours(24));
            linkedJournal.addEntry(linkedEntry);

            em.persist(linkedResident);

            Resident unlinkedResident = new Resident();
            unlinkedResident.setFirstName("Bent");
            unlinkedResident.setLastName("Berg");
            unlinkedResident.setCprNr("020202-5678");

            Journal unlinkedJournal = new Journal();
            unlinkedJournal.setEntries(new ArrayList<>());
            unlinkedJournal.setResident(unlinkedResident);
            unlinkedResident.setJournal(unlinkedJournal);

            JournalEntry unlinkedEntry = new JournalEntry();
            unlinkedEntry.setAuthor(alice);
            unlinkedEntry.setJournal(unlinkedJournal);
            unlinkedEntry.setTitle("Unlinked resident note");
            unlinkedEntry.setContent("Guardian should NOT be allowed to see this note.");
            unlinkedEntry.setEntryType(EntryType.NOTE);
            unlinkedEntry.setRiskAssessment(RiskAssessment.LOW);
            unlinkedEntry.setCreatedAt(LocalDateTime.now());
            unlinkedEntry.setUpdatedAt(LocalDateTime.now());
            unlinkedEntry.setEditCloseTime(LocalDateTime.now().plusHours(24));
            unlinkedJournal.addEntry(unlinkedEntry);

            em.persist(unlinkedResident);


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
