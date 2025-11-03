package com.carebridge.populator;

import com.carebridge.models.User;
import com.carebridge.models.Resident;
import com.carebridge.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DataPopulator {

    public static void populate() {
        System.out.println("⚙️ Running DataPopulator...");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // --- Check if there's already data ---
            Long userCount = session.createQuery("select count(u) from User u", Long.class).uniqueResult();
            Long residentCount = session.createQuery("select count(r) from Resident r", Long.class).uniqueResult();

            // --- Insert a test user if none exists ---
            if (userCount == 0) {
                User user = new User("Anna Careworker", "anna@carebridge.dk");
                session.persist(user);
                System.out.println("✅ Added test User: " + user.getName());
            }

            // --- Insert a test resident if none exists ---
            if (residentCount == 0) {
                Resident resident = new Resident();
                // If you don't add @GeneratedValue, you MUST set an ID manually:
                // resident.setId(1L);
                session.persist(resident);
                System.out.println("✅ Added test Resident with auto-generated ID");
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("✅ DataPopulator complete.");
    }
}
