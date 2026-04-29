package com.carebridge.config;

import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Populator {

    @PersistenceContext
    private EntityManager em;

    @Value("${carebridge.admin.password}")
    private String adminPassword;

    @Transactional
    public void populate() {
        // Create Admin User
        if (readByEmail("admin@carebridge.io") == null) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@carebridge.io");
            admin.setPassword(adminPassword);
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
}
