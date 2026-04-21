package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class ShiftDAO implements IDAO<Shift, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ShiftDAO.class);
	private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
	private static ShiftDAO instance;

	private ShiftDAO() {
	}

	public static synchronized ShiftDAO getInstance() {
		if (instance == null) instance = new ShiftDAO();
		return instance;
	}

	private EntityManager em() {
		return emf.createEntityManager();
	}

	@Override
	public Shift read(Long id) {
		EntityManager em = em();
		try { return em.find(Shift.class, id); }
		catch (Exception e) { logger.error("Error reading Shift id={}", id, e); throw new ApiRuntimeException(500, "Error reading shift: " + e.getMessage()); }
		finally { em.close(); }
	}

	@Override
	public List<Shift> readAll() {
		EntityManager em = em();
		try { return em.createQuery("SELECT s FROM Shift s ORDER BY s.startShift", Shift.class).getResultList(); }
		catch (Exception e) { logger.error("Error reading all shifts", e); throw new ApiRuntimeException(500, "Error reading all shifts: " + e.getMessage()); }
		finally { em.close(); }
	}

	@Override
	public Shift create(Shift shift) {
		if (shift == null) throw new ApiRuntimeException(400, "Shift cannot be null");
		if (shift.getStartShift() == null) throw new ApiRuntimeException(400, "startShift is required");
		if (shift.getEndShift() == null) throw new ApiRuntimeException(400, "endShift is required");
		if (!shift.getEndShift().isAfter(shift.getStartShift())) throw new ApiRuntimeException(400, "endShift must be after startShift");
		if (shift.getShiftType() == null || shift.getShiftType().isBlank()) throw new ApiRuntimeException(400, "shiftType is required");
		if (shift.getLocation() == null || shift.getLocation().isBlank()) throw new ApiRuntimeException(400, "location is required");
		if (shift.getStatus() == null || shift.getStatus().isBlank()) throw new ApiRuntimeException(400, "status is required");
		if (shift.getPlanPeriodId() == null) throw new ApiRuntimeException(400, "planPeriodId is required");
		if (shift.getAssignedUserId() == null) throw new ApiRuntimeException(400, "assignedUserId is required");
		if (shift.getCreatedBy() == null) throw new ApiRuntimeException(400, "createdBy is required");
		if (shift.getCreatedAt() == null) shift.setCreatedAt(LocalDateTime.now());

		EntityManager em = em();
		try {
			em.getTransaction().begin();
			em.persist(shift);
			em.getTransaction().commit();
			logger.info("Shift created: id={}", shift.getId());
			return shift;
		} catch (ApiRuntimeException e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			throw e;
		}
		catch (Exception e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			logger.error("Error creating shift", e);
			throw new ApiRuntimeException(500, "Error creating shift: " + e.getMessage());
		}
		finally { em.close(); }
	}

	@Override
	public Shift update(Long id, Shift updated) {
		if (updated == null) throw new ApiRuntimeException(400, "Shift cannot be null");

		EntityManager em = em();
		try {
			Shift existing = em.find(Shift.class, id);
			if (existing == null) throw new ApiRuntimeException(404, "Shift not found");

			LocalDateTime startShift = updated.getStartShift() != null ? updated.getStartShift() : existing.getStartShift();
			LocalDateTime endShift = updated.getEndShift() != null ? updated.getEndShift() : existing.getEndShift();
			if (startShift == null) throw new ApiRuntimeException(400, "startShift is required");
			if (endShift == null) throw new ApiRuntimeException(400, "endShift is required");
			if (!endShift.isAfter(startShift)) throw new ApiRuntimeException(400, "endShift must be after startShift");
			if (updated.getShiftType() != null && updated.getShiftType().isBlank()) throw new ApiRuntimeException(400, "shiftType is required");
			if (updated.getLocation() != null && updated.getLocation().isBlank()) throw new ApiRuntimeException(400, "location is required");
			if (updated.getStatus() != null && updated.getStatus().isBlank()) throw new ApiRuntimeException(400, "status is required");

			em.getTransaction().begin();
			if (updated.getShiftType() != null) existing.setShiftType(updated.getShiftType());
			if (updated.getLocation() != null) existing.setLocation(updated.getLocation());
			if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
			if (updated.getPlanPeriodId() != null) existing.setPlanPeriodId(updated.getPlanPeriodId());
			if (updated.getAssignedUserId() != null) existing.setAssignedUserId(updated.getAssignedUserId());
			if (updated.getCreatedBy() != null) existing.setCreatedBy(updated.getCreatedBy());
			if (updated.getStartShift() != null) existing.setStartShift(updated.getStartShift());
			if (updated.getEndShift() != null) existing.setEndShift(updated.getEndShift());

			em.getTransaction().commit();
			logger.info("Shift updated: id={}", id);
			return existing;
		} catch (ApiRuntimeException e) { throw e; }
		catch (Exception e) { logger.error("Error updating shift id = " + id, e); throw new ApiRuntimeException(500, "Error updating shift: " + e.getMessage()); }
		finally { em.close(); }
	}

	@Override
	public void delete(Long id) {
		EntityManager em = em();
		try {
			Shift shift = em.find(Shift.class, id);
			if (shift == null) throw new ApiRuntimeException(404, "Shift not found");
			em.getTransaction().begin();
			em.remove(shift);
			em.getTransaction().commit();
			logger.info("Shift deleted: id={}", id);
		} catch (ApiRuntimeException e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			throw e;
		}
		catch (Exception e) {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			logger.error("Error deleting shift id=" + id, e);
			throw new ApiRuntimeException(500, "Error deleting shift: " + e.getMessage());
		}
		finally { em.close(); }
	}
}