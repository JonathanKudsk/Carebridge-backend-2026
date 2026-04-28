package com.carebridge.config;

import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Populator {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void populate() {
       
        // Create Admin User
        if (readByEmail("admin@carebridge.io") == null) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@carebridge.io");
            admin.setPassword("admin");
            admin.setRole(Role.ADMIN);
            em.persist(admin);
        }
    }

   

    private User readByEmail(String email) {
        var list = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    // Legacy static method for manual calls if needed (but now we have the component)
    public static void populate(jakarta.persistence.EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            // This is messy but keeps legacy code working for a moment
            new PopulatorManual(em).populate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    private static class PopulatorManual {
        private final EntityManager em;
        public PopulatorManual(EntityManager em) { this.em = em; }
        public void populate() {
             // Redundant, using Spring-managed Populator component instead
        }
    }
}
