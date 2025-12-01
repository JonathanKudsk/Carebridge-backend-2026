package com.carebridge.dao.security;

import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class SecurityDAO implements ISecurityDAO {

    private final EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    @Override
    public User getVerifiedUser(String email, String password) throws ValidationException {
        try (var em = em()) {
            User user = findByEmail(em, email);
            if (user == null) throw new ValidationException("Invalid email or password");
            if (!user.verifyPassword(password)) throw new ValidationException("Invalid email or password");
            return user;
        }
    }

    @Override
    public User createUser(
            String name,
            String email,
            String rawPassword,
            String displayName,
            String displayEmail,
            String displayPhone,
            String internalEmail,
            String internalPhone,
            Role role
    ) {
        try (var em = em()) {
            if (findByEmail(em, email) != null) {
                throw new ApiRuntimeException(409, "Email already exists");
            }


            var user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(rawPassword);
            user.setRole(role != null ? role : Role.USER);
            user.setDisplayName(displayName);
            user.setDisplayEmail(displayEmail);
            user.setDisplayPhone(displayPhone);
            user.setInternalEmail(internalEmail);
            user.setInternalPhone(internalPhone);

            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiRuntimeException(400, ex.getMessage());
        }
    }


    @Override
    public User changeRole(Long userId, Role newRole) {
        if (newRole == null) throw new ApiRuntimeException(400, "Role must not be null");
        try (var em = em()) {
            em.getTransaction().begin();
            User u = em.find(User.class, userId);
            if (u == null) {
                em.getTransaction().rollback();
                throw new ApiRuntimeException(404, "User not found: id=" + userId);
            }
            u.setRole(newRole);
            em.getTransaction().commit();
            return u;
        }
    }

    private User findByEmail(EntityManager em, String email) {
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
