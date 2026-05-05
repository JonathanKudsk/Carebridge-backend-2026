package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.ShiftAssignment;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ShiftAssignmentDAO implements IDAO<ShiftAssignment, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ShiftAssignmentDAO.class);
	private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
	private static ShiftAssignmentDAO instance;

	private ShiftAssignmentDAO() {
	}

	public static synchronized ShiftAssignmentDAO getInstance() {
		if (instance == null) instance = new ShiftAssignmentDAO();
		return instance;
	}

	private EntityManager em() {
		return emf.createEntityManager();
	}

	@Override
	public ShiftAssignment read(Long id) {
		try (var em = em()) {
			return em.find(ShiftAssignment.class, id);
		} catch (Exception e) {
			logger.error("Error reading shift assignment {}", id, e);
			throw new ApiRuntimeException(500, "Error reading shift assignment: " + e.getMessage());
		}
	}

	@Override
	public List<ShiftAssignment> readAll() {
		try (var em = em()) {
			return em.createQuery("SELECT s FROM ShiftAssignment s ORDER BY s.assignedAt", ShiftAssignment.class).getResultList();
		} catch (Exception e) {
			logger.error("Error reading all shift assignments", e);
			throw new ApiRuntimeException(500, "Error reading shift assignments: " + e.getMessage());
		}
	}

	public List<ShiftAssignment> readByUser(Long userId) {
		if (userId == null) throw new ApiRuntimeException(400, "userId is required");
		try (var em = em()) {
			return em.createQuery("SELECT s FROM ShiftAssignment s WHERE s.userId = :userId ORDER BY s.assignedAt", ShiftAssignment.class)
					.setParameter("userId", userId)
					.getResultList();
		} catch (Exception e) {
			logger.error("Error reading shift assignments by user {}", userId, e);
			throw new ApiRuntimeException(500, "Error reading shift assignments by user: " + e.getMessage());
		}
	}

	public List<ShiftAssignment> readByShift(Long shiftId) {
		if (shiftId == null) throw new ApiRuntimeException(400, "shiftId is required");
		try (var em = em()) {
			return em.createQuery("SELECT s FROM ShiftAssignment s WHERE s.shiftId = :shiftId ORDER BY s.assignedAt", ShiftAssignment.class)
					.setParameter("shiftId", shiftId)
					.getResultList();
		} catch (Exception e) {
			logger.error("Error reading shift assignments by shift {}", shiftId, e);
			throw new ApiRuntimeException(500, "Error reading shift assignments by shift: " + e.getMessage());
		}
	}

	@Override
	public ShiftAssignment create(ShiftAssignment shiftAssignment) {
		if (shiftAssignment == null) throw new ApiRuntimeException(400, "ShiftAssignment cannot be null");
		if (shiftAssignment.getShiftId() == null) throw new ApiRuntimeException(400, "shiftId is required");
		if (shiftAssignment.getUserId() == null) throw new ApiRuntimeException(400, "userId is required");
		if (shiftAssignment.getAssignedBy() == null) throw new ApiRuntimeException(400, "assignedBy is required");

		EntityManager em = em();
		try {
			em.getTransaction().begin();
			em.persist(shiftAssignment);
			em.getTransaction().commit();
			return shiftAssignment;
		} catch (ApiRuntimeException e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			throw e;
		} catch (Exception e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			logger.error("Error creating shift assignment", e);
			throw new ApiRuntimeException(500, "Error creating shift assignment: " + e.getMessage());
		} finally {
			em.close();
		}
	}

	@Override
	public ShiftAssignment update(Long id, ShiftAssignment updated) {
		if (updated == null) throw new ApiRuntimeException(400, "ShiftAssignment cannot be null");

		EntityManager em = em();
		try {
			em.getTransaction().begin();
			ShiftAssignment existing = em.find(ShiftAssignment.class, id);
			if (existing == null) throw new ApiRuntimeException(404, "ShiftAssignment not found");

			if (updated.getShiftId() != null) existing.setShiftId(updated.getShiftId());
			if (updated.getUserId() != null) existing.setUserId(updated.getUserId());
			if (updated.getAssignedAt() != null) existing.setAssignedAt(updated.getAssignedAt());
			if (updated.getAssignedBy() != null) existing.setAssignedBy(updated.getAssignedBy());

			em.getTransaction().commit();
			return existing;
		} catch (ApiRuntimeException e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			throw e;
		} catch (Exception e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			logger.error("Error updating shift assignment {}", id, e);
			throw new ApiRuntimeException(500, "Error updating shift assignment: " + e.getMessage());
		} finally {
			em.close();
		}
	}

	@Override
	public void delete(Long id) {
		EntityManager em = em();
		try {
			em.getTransaction().begin();
			ShiftAssignment existing = em.find(ShiftAssignment.class, id);
			if (existing == null) throw new ApiRuntimeException(404, "ShiftAssignment not found");
			em.remove(existing);
			em.getTransaction().commit();
		} catch (ApiRuntimeException e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			throw e;
		} catch (Exception e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			logger.error("Error deleting shift assignment {}", id, e);
			throw new ApiRuntimeException(500, "Error deleting shift assignment: " + e.getMessage());
		} finally {
			em.close();
		}
	}
}
