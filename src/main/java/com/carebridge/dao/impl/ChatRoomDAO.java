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

import java.util.ArrayList;
import java.util.List;

public class ChatRoomDAO implements IDAO<ChatRoom, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ChatRoomDAO.class);
	private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
	private static ChatRoomDAO instance;

	private ChatRoomDAO() {
	}

	public static synchronized ChatRoomDAO getInstance() {
		if (instance == null) instance = new ChatRoomDAO();
		return instance;
	}

	private EntityManager em() {
		return emf.createEntityManager();
	}

	@Override
	public ChatRoom read(Long id) {
		try (var em = em()) {
			// Fetch members and users in one query so the controller can serialize the room safely.
			var list = em.createQuery(
							"SELECT DISTINCT c FROM ChatRoom c " +
									"LEFT JOIN FETCH c.chatRoomUser cru " +
									"LEFT JOIN FETCH cru.user " +
									"WHERE c.id = :id",
							ChatRoom.class
					)
					.setParameter("id", id)
					.getResultList();

			return list.isEmpty() ? null : list.getFirst();
		} catch (Exception e) {
			logger.error("Error reading chat room {}", id, e);
			throw new ApiRuntimeException(500, "Error reading chat room: " + e.getMessage());
		}
	}

	@Override
	public List<ChatRoom> readAll() {
		try (var em = em()) {
			// Same fetch strategy here to avoid lazy-loading issues in list responses.
			return em.createQuery(
							"SELECT DISTINCT c FROM ChatRoom c " +
									"LEFT JOIN FETCH c.chatRoomUser cru " +
									"LEFT JOIN FETCH cru.user " +
									"ORDER BY c.id",
							ChatRoom.class
					)
					.getResultList();
		} catch (Exception e) {
			logger.error("Error fetching chat rooms", e);
			throw new ApiRuntimeException(500, "Error fetching chat rooms: " + e.getMessage());
		}
	}

	@Override
	public ChatRoom create(ChatRoom chatRoom) {
		if (chatRoom == null) {
			throw new ApiRuntimeException(400, "Chat room cannot be null");
		}

		try (var em = em()) {
			em.getTransaction().begin();
			em.persist(chatRoom);

			List<ChatRoomUser> members = chatRoom.getChatRoomUser();
			if (members != null) {
				for (ChatRoomUser member : members) {
					validateMember(member);
					User managedUser = em.getReference(User.class, member.getUser().getId());
					member.setUser(managedUser);
					member.setChatRoom(chatRoom);
					em.persist(member);
				}
			}

			em.getTransaction().commit();
			logger.info("Chat room created: id={}", chatRoom.getId());
			return chatRoom;
		} catch (ApiRuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error creating chat room", e);
			throw new ApiRuntimeException(500, "Error creating chat room: " + e.getMessage());
		}
	}

	@Override
	public ChatRoom update(Long id, ChatRoom updated) {
		if (updated == null) {
			throw new ApiRuntimeException(400, "Chat room cannot be null");
		}

		try (var em = em()) {
			em.getTransaction().begin();
			ChatRoom existing = em.find(ChatRoom.class, id);
			if (existing == null) {
				throw new ApiRuntimeException(404, "Chat room not found");
			}

			// Updating a room replaces the full membership list.
			if (updated.getChatRoomUser() != null) {
				em.createQuery("DELETE FROM ChatRoomUser cru WHERE cru.chatRoom.id = :roomId")
						.setParameter("roomId", id)
						.executeUpdate();

				List<ChatRoomUser> newMembers = new ArrayList<>();
				for (ChatRoomUser member : updated.getChatRoomUser()) {
					validateMember(member);
					ChatRoomUser newMember = new ChatRoomUser();
					User managedUser = em.getReference(User.class, member.getUser().getId());
					newMember.setUser(managedUser);
					newMember.setChatRoom(existing);
					em.persist(newMember);
					newMembers.add(newMember);
				}
				existing.setChatRoomUser(newMembers);
			}

			em.getTransaction().commit();
			logger.info("Chat room updated: id={}", id);
			return existing;
		} catch (ApiRuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error updating chat room {}", id, e);
			throw new ApiRuntimeException(500, "Error updating chat room: " + e.getMessage());
		}
	}

	@Override
	public void delete(Long id) {
		try (var em = em()) {
			em.getTransaction().begin();
			ChatRoom existing = em.find(ChatRoom.class, id);
			if (existing == null) {
				throw new ApiRuntimeException(404, "Chat room not found");
			}

			// Remove join-table rows first so the room can be deleted cleanly.
			em.createQuery("DELETE FROM ChatRoomUser cru WHERE cru.chatRoom.id = :roomId")
					.setParameter("roomId", id)
					.executeUpdate();
			em.remove(existing);
			em.getTransaction().commit();
			logger.info("Chat room deleted: id={}", id);
		} catch (ApiRuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error deleting chat room {}", id, e);
			throw new ApiRuntimeException(500, "Error deleting chat room: " + e.getMessage());
		}
	}

	private void validateMember(ChatRoomUser member) {
		if (member == null || member.getUser() == null || member.getUser().getId() == null) {
			throw new ApiRuntimeException(400, "Each chat room member must reference an existing user id");
		}
	}
}
