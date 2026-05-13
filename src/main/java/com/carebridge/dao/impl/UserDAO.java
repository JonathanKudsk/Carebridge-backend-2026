package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDAO implements IDAO<User, Long> {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static UserDAO instance;

    private UserDAO() {
    }

    public static synchronized UserDAO getInstance() {
        if (instance == null) instance = new UserDAO();
        return instance;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    @Override
    public User read(Long id) {
        try (var em = em()) {
            return em.find(User.class, id);
        } catch (Exception e) {
            logger.error("Error reading user {}", id, e);
            throw new ApiRuntimeException(500, "Error reading user: " + e.getMessage());
        }
    }

    public User readByEmail(String email) {
        try (var em = em()) {
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
        try (var em = em()) {
            return em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class).getResultList();
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            throw new ApiRuntimeException(500, "Error fetching users: " + e.getMessage());
        }
    }

    @Override
    public User create(User u) {
        if (u == null) throw new ApiRuntimeException(400, "User cannot be null");
        if (u.getEmail() == null || u.getEmail().isBlank())
            throw new ApiRuntimeException(400, "Email is required");
        if (u.getName() == null || u.getName().isBlank())
            throw new ApiRuntimeException(400, "Name is required");

        if (u.getRole() == null)
            u.setRole(Role.USER);

        try (var em = em()) {
            em.getTransaction().begin();
            boolean exists = !em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", u.getEmail())
                    .getResultList().isEmpty();
            if (exists)
                throw new ApiRuntimeException(409, "Email already exists");

            em.persist(u);
            em.getTransaction().commit();
            logger.info("User created: {}", u.getEmail());
            return u;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating user {}", u.getEmail(), e);
            throw new ApiRuntimeException(500, "Error creating user: " + e.getMessage());
        }
    }

    @Override
    public User update(Long id, User updated) {
        try (var em = em()) {
            em.getTransaction().begin();
            User existing = em.find(User.class, id);
            if (existing == null)
                throw new ApiRuntimeException(404, "User not found");

            if (updated.getName() != null && !updated.getName().isBlank())
                existing.setName(updated.getName());
            if (updated.getEmail() != null && !updated.getEmail().isBlank())
                existing.setEmail(updated.getEmail());
            if (updated.getRole() != null)
                existing.setRole(updated.getRole());

            em.getTransaction().commit();
            logger.info("User updated: id={}", id);
            return existing;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user {}", id, e);
            throw new ApiRuntimeException(500, "Error updating user: " + e.getMessage());
        }
    }

    public List<Long> findUserIdsByRole(Role role) {
        try (var em = em()) {
            return em.createQuery("SELECT u.id FROM User u WHERE u.role = :role", Long.class)
                    .setParameter("role", role)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error fetching user IDs for role {}", role, e);
            throw new ApiRuntimeException(500, "Error fetching users by role: " + e.getMessage());
        }
    }

    /** Removes all guardian–resident links for this user (guardian_residents join table). */
    public void clearResidents(Long guardianId) {
        try (var em = em()) {
            em.getTransaction().begin();
            User guardian = em.find(User.class, guardianId);
            if (guardian != null) {
                guardian.getResidents().clear();
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error clearing residents for guardian {}", guardianId, e);
            throw new ApiRuntimeException(500, "Error clearing residents: " + e.getMessage());
        }
    }

    /** Adds residents to the guardian's {@code guardian_residents} association (idempotent per resident). */
    public void linkResidents(Long guardianId, List<Resident> residentsToLink) {
        if (residentsToLink == null || residentsToLink.isEmpty())
            return;
        try (var em = em()) {
            em.getTransaction().begin();
            User guardian = em.find(User.class, guardianId);
            if (guardian == null)
                throw new ApiRuntimeException(404, "User not found");
            for (Resident r : residentsToLink) {
                if (r == null || r.getId() == null)
                    continue;
                Resident attached = em.find(Resident.class, r.getId());
                if (attached != null && !guardian.getResidents().contains(attached)) {
                    guardian.getResidents().add(attached);
                }
            }
            em.getTransaction().commit();
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error linking residents to guardian {}", guardianId, e);
            throw new ApiRuntimeException(500, "Error linking residents: " + e.getMessage());
        }
    }

    // Returns the ID of the first guardian linked to the given resident, or empty if none.
    public Optional<Long> findGuardianIdByResidentId(Long residentId) {
        try (var em = em()) {
            List<Long> results = em.createQuery(
                    "SELECT u.id FROM User u JOIN u.residents r WHERE r.id = :residentId", Long.class)
                    .setParameter("residentId", residentId)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error fetching guardian for resident {}", residentId, e);
            throw new ApiRuntimeException(500, "Error fetching guardian: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            em.getTransaction().begin();
            User u = em.find(User.class, id);
            if (u == null)
                throw new ApiRuntimeException(404, "User not found");
            em.remove(u);
            em.getTransaction().commit();
            logger.info("User deleted: id={}", id);
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting user {}", id, e);
            throw new ApiRuntimeException(500, "Error deleting user: " + e.getMessage());
        }
    }
}
