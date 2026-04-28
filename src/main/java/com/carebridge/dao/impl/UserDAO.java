package com.carebridge.dao.impl;

import com.carebridge.dao.IDAO;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class UserDAO implements IDAO<User, Long> {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    @PersistenceContext
    private EntityManager em;

    public UserDAO() {
    }

    @Override
    public User read(Long id) {
        try {
            var list = em.createQuery(
                "SELECT DISTINCT u FROM User u " +
                "LEFT JOIN FETCH u.residents " +
                "WHERE u.id = :id", User.class)
                .setParameter("id", id)
                .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            logger.error("Error reading user {}", id, e);
            throw new ApiRuntimeException(500, "Error reading user: " + e.getMessage());
        }
    }

    public User readByEmail(String email) {
        try {
            if (email == null || email.isBlank())
                throw new ValidationException("Email cannot be blank");

            var list = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getResultList();

            return list.isEmpty() ? null : list.get(0);
        } catch (ValidationException e) {
            throw new ApiRuntimeException(400, e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching user by email {}", email, e);
            throw new ApiRuntimeException(500, "Error fetching user: " + e.getMessage());
        }
    }

    @Override
    public List<User> readAll() {
        try {
            return em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class).getResultList();
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            throw new ApiRuntimeException(500, "Error fetching users: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public User create(User u) {
        if (u == null) throw new ApiRuntimeException(400, "User cannot be null");
        if (u.getEmail() == null || u.getEmail().isBlank()){
            throw new ApiRuntimeException(400, "Email is required");
        }
        if (u.getName() == null || u.getName().isBlank()){
            throw new ApiRuntimeException(400, "Name is required");
        }

        boolean exists = !em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", u.getEmail())
                .getResultList().isEmpty();
        if (exists)
            throw new ApiRuntimeException(409, "Email already exists");

        em.persist(u);
        logger.info("User created: {}", u.getEmail());
        return u;
    }

    @Override
    @Transactional
    public User update(Long id, User updated) {
        User existing = em.find(User.class, id);
        if (existing == null)
            throw new ApiRuntimeException(404, "User not found");

        if (updated.getName() != null && !updated.getName().isBlank())
            existing.setName(updated.getName());
        if (updated.getEmail() != null && !updated.getEmail().isBlank())
            existing.setEmail(updated.getEmail());
        if (updated.getRole() != null)
            existing.setRole(updated.getRole());

        logger.info("User updated: id={}", id);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User u = em.find(User.class, id);
        if (u == null)
            throw new ApiRuntimeException(404, "User not found");
        em.remove(u);
        logger.info("User deleted: id={}", id);
    }
}
