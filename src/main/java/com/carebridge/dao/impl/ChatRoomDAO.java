package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChatRoomDAO implements IDAO<ChatRoom, Long> {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static ChatRoomDAO instance;



    public static synchronized ChatRoomDAO getInstance() {
        if (instance == null) instance = new ChatRoomDAO();
        return instance;
    }

    private EntityManager em (){
            return emf.createEntityManager();
      }


    @Override
    public ChatRoom read(Long id) {
        try (var em = em()) {
            return em.find(ChatRoom.class, id);
        } catch (Exception e) {
            logger.error("Error reading Chatroom id={}", id, e);
            throw new ApiRuntimeException(500, "Error reading Chatroom: " + e.getMessage());
        }
    }

    @Override
    public List<ChatRoom> readAll()
    {
        try (var em = em())
        {
            List<ChatRoom> chatRooms = em.createQuery("SELECT c FROM ChatRoom c", ChatRoom.class).getResultList();

            return chatRooms;
        }
    }

    @Override
    public ChatRoom create(ChatRoom chatRoom) {
        try (var em = em())
        {
            em.getTransaction().begin();
            em.persist(chatRoom);
            em.getTransaction().commit();
            return chatRoom;
        }
        catch (Exception e)
        {
            logger.error("Error persisting object to db", e);
            throw new RuntimeException("Error persisting object to db. ", e);
        }
    }

    @Override
    public ChatRoom update(Long id, ChatRoom chatRoom) {
        try(var em = em()){
            em.getTransaction().begin();
            ChatRoom updated = em.merge(chatRoom);
            em.getTransaction().commit();
            return  updated;
        } catch (Exception e) {
            logger.error("Error updating object in db", e);
            throw new RuntimeException("Error updating object in db. ", e);
        }

    }

    @Override
    public void delete(Long id) {
        try(var em = em()){
            em.getTransaction().begin();
            ChatRoom c = em.find(ChatRoom.class, id);
            if(c == null) throw new ApiRuntimeException(404, "Chatroom was not found");
            em.remove(c);
            em.getTransaction().commit();
            logger.info("Chatroom deleted: id={}", id);

        }
    }
}
