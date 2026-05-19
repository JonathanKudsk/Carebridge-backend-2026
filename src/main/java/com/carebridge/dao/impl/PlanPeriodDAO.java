package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.PlanPeriod;
import com.carebridge.entities.enums.PlanStatus;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PlanPeriodDAO implements IDAO<PlanPeriod, Long> {

    private static final Logger logger = LoggerFactory.getLogger(PlanPeriodDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static PlanPeriodDAO instance;

    private PlanPeriodDAO() {
    }

    public static synchronized PlanPeriodDAO getInstance() {
        if (instance == null) instance = new PlanPeriodDAO();
        return instance;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    @Override
    public PlanPeriod read(Long id) {
        EntityManager em = em();
        try { return em.find(PlanPeriod.class, id); }
        catch (Exception e) { logger.error("Error reading PlanPeriod id={}", id, e); throw new ApiRuntimeException(500, "Error reading plan period: " + e.getMessage()); }
        finally { em.close(); }
    }

    @Override
    public List<PlanPeriod> readAll() {
        EntityManager em = em();
        try { return em.createQuery("SELECT p FROM PlanPeriod p ORDER BY p.startDate", PlanPeriod.class).getResultList(); }
        catch (Exception e) { logger.error("Error reading all plan periods", e); throw new ApiRuntimeException(500, "Error reading all plan periods: " + e.getMessage()); }
        finally { em.close(); }
    }

    public List<PlanPeriod> readActive() {
        EntityManager em = em();
        try {
            return em.createQuery("SELECT p FROM PlanPeriod p WHERE p.status = :status ORDER BY p.startDate", PlanPeriod.class)
                    .setParameter("status", PlanStatus.PUBLISHED)
                    .getResultList();
        }
        catch (Exception e) { logger.error("Error reading active plan periods", e); throw new ApiRuntimeException(500, "Error reading active plan periods: " + e.getMessage()); }
        finally { em.close(); }
    }

    @Override
    public PlanPeriod create(PlanPeriod planPeriod) {
        if (planPeriod == null) throw new ApiRuntimeException(400, "PlanPeriod cannot be null");
        if (planPeriod.getStartDate() == null) throw new ApiRuntimeException(400, "startDate is required");
        if (planPeriod.getEndDate() == null) throw new ApiRuntimeException(400, "endDate is required");
        if (!planPeriod.getEndDate().isAfter(planPeriod.getStartDate())) throw new ApiRuntimeException(400, "endDate must be after startDate");
        if (planPeriod.getCreatedBy() == null) throw new ApiRuntimeException(400, "createdBy is required");
        if (planPeriod.getStatus() == null) planPeriod.setStatus(PlanStatus.DRAFT);
        if (planPeriod.getCreatedAt() == null) planPeriod.setCreatedAt(LocalDateTime.now());

        EntityManager em = em();
        try {
            em.getTransaction().begin();
            em.persist(planPeriod);
            em.getTransaction().commit();
            logger.info("PlanPeriod created: id={}", planPeriod.getId());
            return planPeriod;
        } catch (ApiRuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Error creating plan period", e);
            throw new ApiRuntimeException(500, "Error creating plan period: " + e.getMessage());
        } finally { em.close(); }
    }

    @Override
    public PlanPeriod update(Long id, PlanPeriod updated) {
        if (updated == null) throw new ApiRuntimeException(400, "PlanPeriod cannot be null");

        EntityManager em = em();
        try {
            PlanPeriod existing = em.find(PlanPeriod.class, id);
            if (existing == null) throw new ApiRuntimeException(404, "PlanPeriod not found");
            if (existing.getStatus() == PlanStatus.PUBLISHED) throw new ApiRuntimeException(400, "Cannot update a published plan period");

            LocalDate startDate = updated.getStartDate() != null ? updated.getStartDate() : existing.getStartDate();
            LocalDate endDate = updated.getEndDate() != null ? updated.getEndDate() : existing.getEndDate();
            if (!endDate.isAfter(startDate)) throw new ApiRuntimeException(400, "endDate must be after startDate");

            em.getTransaction().begin();
            if (updated.getStartDate() != null) existing.setStartDate(updated.getStartDate());
            if (updated.getEndDate() != null) existing.setEndDate(updated.getEndDate());
            if (updated.getCreatedBy() != null) existing.setCreatedBy(updated.getCreatedBy());
            em.getTransaction().commit();
            logger.info("PlanPeriod updated: id={}", id);
            return existing;
        } catch (ApiRuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Error updating plan period id={}", id, e);
            throw new ApiRuntimeException(500, "Error updating plan period: " + e.getMessage());
        } finally { em.close(); }
    }

    public PlanPeriod publish(Long id) {
        EntityManager em = em();
        try {
            PlanPeriod existing = em.find(PlanPeriod.class, id);
            if (existing == null) throw new ApiRuntimeException(404, "PlanPeriod not found");
            if (existing.getStatus() == PlanStatus.PUBLISHED) throw new ApiRuntimeException(400, "PlanPeriod is already published");

            em.getTransaction().begin();
            existing.setStatus(PlanStatus.PUBLISHED);
            em.getTransaction().commit();
            logger.info("PlanPeriod published: id={}", id);
            return existing;
        } catch (ApiRuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Error publishing plan period id={}", id, e);
            throw new ApiRuntimeException(500, "Error publishing plan period: " + e.getMessage());
        } finally { em.close(); }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = em();
        try {
            PlanPeriod planPeriod = em.find(PlanPeriod.class, id);
            if (planPeriod == null) throw new ApiRuntimeException(404, "PlanPeriod not found");
            if (planPeriod.getStatus() == PlanStatus.PUBLISHED) throw new ApiRuntimeException(400, "Cannot delete a published plan period");
            em.getTransaction().begin();
            em.remove(planPeriod);
            em.getTransaction().commit();
            logger.info("PlanPeriod deleted: id={}", id);
        } catch (ApiRuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Error deleting plan period id={}", id, e);
            throw new ApiRuntimeException(500, "Error deleting plan period: " + e.getMessage());
        } finally { em.close(); }
    }
}