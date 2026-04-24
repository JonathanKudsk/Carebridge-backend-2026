package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.entities.Message;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageDAO implements IDAO<Message, Long> {

    private static final Logger logger = LoggerFactory.getLogger(MessageDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static MessageDAO instance;

    private MessageDAO() {
    }

    public static synchronized MessageDAO getInstance() {
        if (instance == null) instance = new MessageDAO();
        return instance;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }


    @Override
    public Message read(Long id) {
        try (var em = em()) {
            // Join-fetch the related user and room so message serialization stays safe.
            var list = em.createQuery(
                            "SELECT m FROM Message m " +
                                    "JOIN FETCH m.user " +
                                    "JOIN FETCH m.chatRoom " +
                                    "WHERE m.id = :id",
                            Message.class
                    )
                    .setParameter("id", id)
                    .getResultList();
            return list.isEmpty() ? null : list.getFirst();
        } catch (Exception e) {
            logger.error("Error reading message {}", id, e);
            throw new ApiRuntimeException(500, "Error reading message: " + e.getMessage());
        }
    }

    @Override
    public List<Message> readAll() {
        try (var em = em()) {
            // Keep ordering stable for the REST tests and the message list UI.
            return em.createQuery(
                            "SELECT m FROM Message m " +
                                    "JOIN FETCH m.user " +
                                    "JOIN FETCH m.chatRoom " +
                                    "ORDER BY m.timestamp, m.id",
                            Message.class
                    )
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error fetching all messages", e);
            throw new ApiRuntimeException(500, "Error fetching messages: " + e.getMessage());
        }
    }

    public List<Message> readByChatRoom(Long chatRoomId) {
        try (var em = em()) {
            // Filter by room so the frontend can load one conversation thread.
            return em.createQuery(
                            "SELECT m FROM Message m " +
                                    "JOIN FETCH m.user " +
                                    "JOIN FETCH m.chatRoom " +
                                    "WHERE m.chatRoom.id = :chatRoomId " +
                                    "ORDER BY m.timestamp, m.id",
                            Message.class
                    )
                    .setParameter("chatRoomId", chatRoomId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error fetching messages for chat room {}", chatRoomId, e);
            throw new ApiRuntimeException(500, "Error fetching messages: " + e.getMessage());
        }
    }


    @Override
    public Message create(Message message) {
        if (message == null) {
            throw new ApiRuntimeException(400, "Message cannot be null");
        }
        if (message.getUser() == null || message.getUser().getId() == null) {
            throw new ApiRuntimeException(400, "Message user is required");
        }
        if (message.getChatRoom() == null || message.getChatRoom().getId() == null) {
            throw new ApiRuntimeException(400, "Message chat room is required");
        }
        if (message.getMessage() == null || message.getMessage().isBlank()) {
            throw new ApiRuntimeException(400, "Message text is required");
        }

        try (var em = em()) {
            em.getTransaction().begin();

            // Check if chat room is active
            ChatRoom chatRoom = em.find(ChatRoom.class, message.getChatRoom().getId());
            if (chatRoom == null)  {
                throw new ApiRuntimeException(404, "Chat room not found");
            }
            if (!chatRoom.isActive())  {
                throw new ApiRuntimeException(403, "This chat room is read-only");
            }

            // Replace detached ids with managed references before persisting.
            User userRef = em.getReference(User.class, message.getUser().getId());
            ChatRoom roomRef = em.getReference(ChatRoom.class, message.getChatRoom().getId());
            message.setUser(userRef);
            message.setChatRoom(roomRef);

            em.persist(message);
            em.getTransaction().commit();
            logger.info("Message created: id={}", message.getId());
            return message;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating message", e);
            throw new ApiRuntimeException(500, "Error creating message: " + e.getMessage());
        }
    }

    @Override
    public Message update(Long id, Message updated) {
        if (updated == null) {
            throw new ApiRuntimeException(400, "Message cannot be null");
        }

        try (var em = em()) {
            em.getTransaction().begin();
            Message existing = em.find(Message.class, id);
            if (existing == null) {
                throw new ApiRuntimeException(404, "Message not found");
            }

            // Only overwrite fields the client actually changed.
            if (updated.getMessage() != null && !updated.getMessage().isBlank()) {
                existing.setMessage(updated.getMessage());
            }
            if (updated.getUser() != null && updated.getUser().getId() != null) {
                existing.setUser(em.getReference(User.class, updated.getUser().getId()));
            }
            if (updated.getChatRoom() != null && updated.getChatRoom().getId() != null) {
                existing.setChatRoom(em.getReference(ChatRoom.class, updated.getChatRoom().getId()));
            }
            if (updated.getTimestamp() != null) {
                existing.setTimestamp(updated.getTimestamp());
            }

            em.getTransaction().commit();
            logger.info("Message updated: id={}", id);
            return existing;
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating message {}", id, e);
            throw new ApiRuntimeException(500, "Error updating message: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            em.getTransaction().begin();
            Message existing = em.find(Message.class, id);
            if (existing == null) {
                throw new ApiRuntimeException(404, "Message not found");
            }
            em.remove(existing);
            em.getTransaction().commit();
            logger.info("Message deleted: id={}", id);
        } catch (ApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting message {}", id, e);
            throw new ApiRuntimeException(500, "Error deleting message: " + e.getMessage());
        }
    }

}
