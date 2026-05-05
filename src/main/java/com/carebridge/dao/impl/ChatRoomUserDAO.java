package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.entities.ChatRoomUser;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChatRoomUserDAO implements IDAO<ChatRoomUser, Long> {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomUserDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static ChatRoomUserDAO instance;

    private ChatRoomUserDAO() {
    }

    public static synchronized ChatRoomUserDAO getInstance() {
        if (instance == null) instance = new ChatRoomUserDAO();
        return instance;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    @Override
    public ChatRoomUser read(Long id) {
        try (var em = em()) {
            // Load the linked user and room together for safe serialization.
            var list = em.createQuery(
                            "SELECT cru FROM ChatRoomUser cru " +
                                    "JOIN FETCH cru.user " +
                                    "JOIN FETCH cru.chatRoom " +
                                    "WHERE cru.id = :id",
                            ChatRoomUser.class
                    )
                    .setParameter("id", id)
                    .getResultList();
                  return list.isEmpty() ? null : list.getFirst();
        } catch (Exception e) {
            logger.error("Error reading chat room user {}", id, e);
            throw new ApiRuntimeException(500, "Error reading chat room user: " + e.getMessage());
        }
    }

    @Override
    public List<ChatRoomUser> readAll() {
        try (var em = em()) {
            // Order by id so repeated test assertions stay deterministic.
            return em.createQuery(
                            "SELECT cru FROM ChatRoomUser cru " +
                                    "JOIN FETCH cru.user " +
                                    "JOIN FETCH cru.chatRoom " +
                                    "ORDER BY cru.id",
                            ChatRoomUser.class
                    )
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading all chat room users", e);
            throw new ApiRuntimeException(500, "Error reading chat room users: " + e.getMessage());
        }
    }

    public List<ChatRoomUser> readByChatRoom(Long chatRoomId) {
        try (var em = em()) {
            // Filter by room for the chat-room-membership endpoint.
            return em.createQuery(
                            "SELECT cru FROM ChatRoomUser cru " +
                                    "JOIN FETCH cru.user " +
                                    "JOIN FETCH cru.chatRoom " +
                                    "WHERE cru.chatRoom.id = :chatRoomId " +
                                    "ORDER BY cru.id",
                            ChatRoomUser.class
                    )
                    .setParameter("chatRoomId", chatRoomId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading members by chat room {}", chatRoomId, e);
            throw new ApiRuntimeException(500, "Error reading chat room members: " + e.getMessage());
        }
    }

    public List<ChatRoomUser> readByUser(Long userId) {
        try (var em = em()) {
            // Filter by user so the frontend can show a user's memberships.
            return em.createQuery(
                            "SELECT cru FROM ChatRoomUser cru " +
                                    "JOIN FETCH cru.user " +
                                    "JOIN FETCH cru.chatRoom " +
                                    "WHERE cru.user.id = :userId " +
                                    "ORDER BY cru.id",
                            ChatRoomUser.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading memberships by user {}", userId, e);
            throw new ApiRuntimeException(500, "Error reading user memberships: " + e.getMessage());
        }
    }

    @Override
    public ChatRoomUser create(ChatRoomUser entity) {
        validate(entity);

        try (var em = em()) {
            em.getTransaction().begin();

            User userRef = em.getReference(User.class, entity.getUser().getId());
            ChatRoom roomRef = em.getReference(ChatRoom.class, entity.getChatRoom().getId());
            entity.setUser(userRef);
            entity.setChatRoom(roomRef);

            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating chat room membership", e);
            throw new ApiRuntimeException(500, "Error creating chat room membership: " + e.getMessage());
        }
    }

    @Override
    public ChatRoomUser update(Long id, ChatRoomUser updated) {
        if (updated == null) {
            throw new ApiRuntimeException(400, "Membership payload cannot be null");
        }

        try (var em = em()) {
            em.getTransaction().begin();
            ChatRoomUser existing = em.find(ChatRoomUser.class, id);
            if (existing == null) {
                throw new ApiRuntimeException(404, "Membership not found");
            }

            // Update the owning side directly so the join row points to the new ids.
            if (updated.getUser() != null && updated.getUser().getId() != null) {
                existing.setUser(em.getReference(User.class, updated.getUser().getId()));
            }
            if (updated.getChatRoom() != null && updated.getChatRoom().getId() != null) {
                existing.setChatRoom(em.getReference(ChatRoom.class, updated.getChatRoom().getId()));
            }

            em.getTransaction().commit();
            return existing;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating chat room membership {}", id, e);
            throw new ApiRuntimeException(500, "Error updating chat room membership: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            em.getTransaction().begin();
            ChatRoomUser existing = em.find(ChatRoomUser.class, id);
            if (existing == null) {
                throw new ApiRuntimeException(404, "Membership not found");
            }

            // The row is removed from the join table before the entity itself is deleted.
            em.remove(existing);
            em.getTransaction().commit();
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting chat room membership {}", id, e);
            throw new ApiRuntimeException(500, "Error deleting chat room membership: " + e.getMessage());
        }
    }

    private void validate(ChatRoomUser entity) {
        if (entity == null) {
            throw new ApiRuntimeException(400, "Membership cannot be null");
        }
        if (entity.getUser() == null || entity.getUser().getId() == null) {
            throw new ApiRuntimeException(400, "Membership user id is required");
        }
        if (entity.getChatRoom() == null || entity.getChatRoom().getId() == null) {
            throw new ApiRuntimeException(400, "Membership chatRoom id is required");
        }
    }
}

