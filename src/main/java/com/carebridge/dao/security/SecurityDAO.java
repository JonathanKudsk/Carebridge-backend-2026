package com.carebridge.dao.security;

import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SecurityDAO implements ISecurityDAO {

    @PersistenceContext
    private EntityManager em;

    public SecurityDAO() {
    }

    @Override
    public User getVerifiedUser(String email, String password) throws ValidationException {
        User user = readByEmail(email);
        if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new ValidationException("Wrong username or password");
        }
        return user;
    }

    @Override
    @Transactional
    public User createUser(String name, String email, String rawPassword, String displayName, String displayEmail, String displayPhone, String internalEmail, String internalPhone, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(rawPassword);
        user.setDisplayName(displayName);
        user.setDisplayEmail(displayEmail);
        user.setDisplayPhone(displayPhone);
        user.setInternalEmail(internalEmail);
        user.setInternalPhone(internalPhone);
        user.setRole(role);
        em.persist(user);
        return user;
    }

    @Override
    @Transactional
    public User changeRole(Long userId, Role newRole) {
        User user = em.find(User.class, userId);
        if (user != null) {
            user.setRole(newRole);
        }
        return user;
    }

    private User readByEmail(String email) {
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
