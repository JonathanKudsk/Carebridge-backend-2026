package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Location;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    public List<User> readAllSubstitutes(Long locationId, String locationName) {
        try (var em = em()) {
            StringBuilder query = new StringBuilder(
                    "SELECT DISTINCT u FROM User u " +
                            "LEFT JOIN FETCH u.locations fetchedLocation " +
                            "WHERE u.role = :role"
            );

            if (locationId != null) {
                query.append(" AND EXISTS (SELECT 1 FROM User filterUser JOIN filterUser.locations filterLocation ")
                        .append("WHERE filterUser = u AND filterLocation.id = :locationId)");
            }

            if (locationName != null && !locationName.isBlank()) {
                query.append(" AND EXISTS (SELECT 1 FROM User filterUser JOIN filterUser.locations filterLocation ")
                        .append("WHERE filterUser = u AND LOWER(filterLocation.locationName) LIKE :locationName)");
            }

            query.append(" ORDER BY u.name");

            var typedQuery = em.createQuery(query.toString(), User.class)
                    .setParameter("role", Role.SUBSTITUTE);

            if (locationId != null) {
                typedQuery.setParameter("locationId", locationId);
            }

            if (locationName != null && !locationName.isBlank()) {
                typedQuery.setParameter("locationName", "%" + locationName.toLowerCase() + "%");
            }

            return typedQuery.getResultList();
        } catch (Exception e) {
            logger.error("Error fetching substitutes", e);
            throw new ApiRuntimeException(500, "Error fetching substitutes: " + e.getMessage());
        }
    }

    public List<Location> readSubstituteLocations() {
        try (var em = em()) {
            return em.createQuery(
                            "SELECT DISTINCT l FROM Location l JOIN l.users u " +
                                    "WHERE u.role = :role ORDER BY l.locationName",
                            Location.class)
                    .setParameter("role", Role.SUBSTITUTE)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error fetching substitute locations", e);
            throw new ApiRuntimeException(500, "Error fetching substitute locations: " + e.getMessage());
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
            if (updated.isEmployed() != existing.isEmployed()) {
                existing.setIsEmployed(updated.isEmployed());
            }

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

    public User linkResidents(Long guardianId, List<Resident> residents) {
        if (guardianId == null) {
            throw new ApiRuntimeException(400, "Guardian id is required");
        }
        if (residents == null) {
            throw new ApiRuntimeException(400, "Residents are required");
        }

        try (var em = em()) {
            em.getTransaction().begin();

            User guardian = em.find(User.class, guardianId);
            if (guardian == null) {
                throw new ApiRuntimeException(404, "Guardian not found");
            }
            if (guardian.getRole() != Role.GUARDIAN) {
                throw new ApiRuntimeException(400, "User is not a guardian");
            }

            for (Resident resident : residents) {
                if (resident != null && resident.getId() != null) {
                    guardian.addResident(em.getReference(Resident.class, resident.getId()));
                }
            }

            em.merge(guardian);
            em.getTransaction().commit();
            return guardian;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error linking residents to guardian {}", guardianId, e);
            throw new ApiRuntimeException(500, "Error linking residents: " + e.getMessage());
        }
    }

    public User clearResidents(Long guardianId) {
        if (guardianId == null) {
            throw new ApiRuntimeException(400, "Guardian id is required");
        }

        try (var em = em()) {
            em.getTransaction().begin();

            User guardian = em.find(User.class, guardianId);
            if (guardian == null) {
                throw new ApiRuntimeException(404, "Guardian not found");
            }

            guardian.getResidents().clear();
            em.merge(guardian);
            em.getTransaction().commit();
            return guardian;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error clearing residents for guardian {}", guardianId, e);
            throw new ApiRuntimeException(500, "Error clearing residents: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            em.getTransaction().begin();
            User u = em.find(User.class, id);
            if (u == null)
                throw new ApiRuntimeException(404, "User not found");

            // Deactivate chat rooms before deleting user making chat read only for the user
            ChatRoomDAO.getInstance().deactivateChatRoomsForUser(id);

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

    public User readWithLocation(Long id) {
        try (var em = em()) {
            var list = em.createQuery("SELECT u FROM User u left join FETCH u.locations where u.id = :id", User.class)
                    .setParameter("id", id)
                    .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            logger.error("Error fetching user by id {}", id, e);
            throw new ApiRuntimeException(500, "Error fetching user: " + e.getMessage());
        }
    }

    public User attachLocationtoUser (Long userId, Long locationId) {
        try (var em = em()) {
            em.getTransaction().begin();
            User u = em.createQuery("SELECT u FROM User u left join FETCH u.locations WHERE u.id = :userId", User.class)
                    .setParameter("userId", userId)
                    .getSingleResult();

            Location l = em.find(Location.class, locationId);
            if (l == null)
                throw new ApiRuntimeException(404, "Location not found");

            u.addLocation(l);

            em.merge(u);

            em.getTransaction().commit();
            logger.info("Location {} attached to user {}", locationId ,userId);
            return u;

        } catch (NoResultException e) { //only thrown by user search query
            throw new ApiRuntimeException(404, "User not found");
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error attaching location {} to user {}",locationId, userId, e);
            throw new ApiRuntimeException(500, "Error attaching location to user: " + e.getMessage());
        }
    }

    public User detachLocationtoUser(Long userId, Long locationId) {
        try (var em = em()) {
            em.getTransaction().begin();
            User u = em.createQuery("SELECT u FROM User u left join FETCH u.locations WHERE u.id = :userId", User.class)
                    .setParameter("userId", userId)
                    .getSingleResult();

            Location l = em.find(Location.class, locationId);
            if (l == null)
                throw new ApiRuntimeException(404, "Location not found");

            u.removeLocation(l);

            em.merge(u);

            em.getTransaction().commit();
            logger.info("Location {} detached to user {}", locationId ,userId);
            return u;

        } catch (NoResultException e) { //only thrown by user search query
            throw new ApiRuntimeException(404, "User not found");
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error detaching location {} to user {}",locationId, userId, e);
            throw new ApiRuntimeException(500, "Error detaching location to user: " + e.getMessage());
        }
    }
}
