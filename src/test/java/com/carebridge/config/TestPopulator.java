package com.carebridge.config;

import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Test-only populator that always resets users to their known initial TOTP state.
 * This prevents test class contamination when all classes share the same static EMF
 * across a full Maven test run (EventTest → EventTypeTest → SecurityTest).
 */
public class TestPopulator {

    private static final Logger logger = LoggerFactory.getLogger(TestPopulator.class);

    public static final String ALICE_TOTP_SECRET   = "JBSWY3DPEHPK3PXQ";
    public static final String ADMIN_TOTP_SECRET   = "JBSWY3DPEHPK3PXP";
    public static final String PARTIAL_TOTP_SECRET = "JBSWY3DPEHPK3PXR";
    public static final String GRACE_TOTP_SECRET   = "JBSWY3DPEHPK3PXS";

    public static void populate(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Admin – always reset to: no TOTP configured (setup required on first login)
            User admin = findByEmail(em, "admin@carebridge.io");
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
            }
            admin.setTotpSecret(null);
            admin.setTotpEnabled(false);
            admin.setTotpGracePeriodEnd(null);
            if (admin.getId() == null) em.persist(admin); else em.merge(admin);

            // Alice – always reset to: TOTP fully enabled, no active grace period
            User alice = findByEmail(em, "alice@carebridge.io");
            if (alice == null) {
                alice = new User();
                alice.setName("Alice");
                alice.setEmail("alice@carebridge.io");
                alice.setPassword("password123");
                alice.setRole(Role.CAREWORKER);
                alice.setDisplayName("Alice");
                alice.setDisplayEmail("alice@carebridge.io");
                alice.setDisplayPhone("111-1111-1111");
                alice.setInternalEmail("alice.internal@carebridge.io");
                alice.setInternalPhone("222-2222-2222");
            }
            alice.setTotpSecret(ALICE_TOTP_SECRET);
            alice.setTotpEnabled(true);
            alice.setTotpGracePeriodEnd(null);
            if (alice.getId() == null) em.persist(alice); else em.merge(alice);

            // no2fa – never configured 2FA (create once, no TOTP state to reset)
            User no2fa = findByEmail(em, "no2fa@carebridge.io");
            if (no2fa == null) {
                no2fa = new User();
                no2fa.setName("No2FA");
                no2fa.setEmail("no2fa@carebridge.io");
                no2fa.setPassword("password123");
                no2fa.setRole(Role.USER);
                no2fa.setDisplayName("No 2FA User");
                no2fa.setDisplayEmail("no2fa@carebridge.io");
                no2fa.setDisplayPhone("333-3333-3333");
                no2fa.setInternalEmail("no2fa.internal@carebridge.io");
                no2fa.setInternalPhone("444-4444-4444");
                em.persist(no2fa);
            }

            // partial – always reset to: secret saved but totp_enabled=false (abandoned setup)
            User partial = findByEmail(em, "partial@carebridge.io");
            if (partial == null) {
                partial = new User();
                partial.setName("Partial");
                partial.setEmail("partial@carebridge.io");
                partial.setPassword("password123");
                partial.setRole(Role.USER);
                partial.setDisplayName("Partial 2FA User");
                partial.setDisplayEmail("partial@carebridge.io");
                partial.setDisplayPhone("555-5555-5555");
                partial.setInternalEmail("partial.internal@carebridge.io");
                partial.setInternalPhone("666-6666-6666");
            }
            partial.setTotpSecret(PARTIAL_TOTP_SECRET);
            partial.setTotpEnabled(false);
            partial.setTotpGracePeriodEnd(null);
            if (partial.getId() == null) em.persist(partial); else em.merge(partial);

            // grace – always reset to: TOTP enabled with a fresh 14-day grace period
            User grace = findByEmail(em, "grace@carebridge.io");
            if (grace == null) {
                grace = new User();
                grace.setName("Grace");
                grace.setEmail("grace@carebridge.io");
                grace.setPassword("password123");
                grace.setRole(Role.USER);
                grace.setDisplayName("Grace Period User");
                grace.setDisplayEmail("grace@carebridge.io");
                grace.setDisplayPhone("777-7777-7777");
                grace.setInternalEmail("grace.internal@carebridge.io");
                grace.setInternalPhone("888-8888-8888");
            }
            grace.setTotpSecret(GRACE_TOTP_SECRET);
            grace.setTotpEnabled(true);
            grace.setTotpGracePeriodEnd(Instant.now().plus(14, ChronoUnit.DAYS));
            if (grace.getId() == null) em.persist(grace); else em.merge(grace);

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
            logger.info("Test database reset and populated successfully.");
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            logger.error("Test population failed", ex);
            throw ex;
        } finally {
            em.close();
        }
    }

    private static User findByEmail(EntityManager em, String email) {
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
