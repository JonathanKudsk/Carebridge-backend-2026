package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Budget;
import com.carebridge.entities.SavingsGoal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SavingsGoalDAO implements IDAO<SavingsGoal, Long> {

    private static final Logger logger = LoggerFactory.getLogger(SavingsGoalDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static SavingsGoalDAO instance;

    public static synchronized SavingsGoalDAO getInstance() {
        if (instance == null) instance = new SavingsGoalDAO();
        return instance;
    }

    @Override
    public SavingsGoal create(SavingsGoal goal) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            if (goal.getBudget() != null && goal.getBudget().getId() != null) {
                goal.setBudget(em.getReference(Budget.class, goal.getBudget().getId()));
            }
            em.persist(goal);
            em.getTransaction().commit();
            return goal;
        } catch (Exception e) {
            logger.error("Error persisting savings goal", e);
            throw new RuntimeException("Error persisting savings goal", e);
        }
    }

    @Override
    public SavingsGoal read(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            SavingsGoal goal = em.find(SavingsGoal.class, id);
            if (goal == null) throw new RuntimeException("SavingsGoal not found with ID: " + id);
            return goal;
        } catch (Exception e) {
            logger.error("Error reading savings goal", e);
            throw new RuntimeException("Error reading savings goal", e);
        }
    }

    @Override
    public List<SavingsGoal> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT s FROM SavingsGoal s", SavingsGoal.class).getResultList();
        }
    }

    @Override
    public SavingsGoal update(Long id, SavingsGoal goal) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SavingsGoal merged = em.merge(goal);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            logger.error("Error updating savings goal", e);
            throw new RuntimeException("Error updating savings goal", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SavingsGoal goal = em.find(SavingsGoal.class, id);
            if (goal != null) em.remove(goal);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error deleting savings goal", e);
            throw new RuntimeException("Error deleting savings goal", e);
        }
    }

    public List<SavingsGoal> findByBudgetId(Long budgetId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT s FROM SavingsGoal s WHERE s.budget.id = :budgetId", SavingsGoal.class)
                    .setParameter("budgetId", budgetId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error finding savings goals by budget id", e);
            throw new RuntimeException("Error finding savings goals by budget id", e);
        }
    }

    public List<SavingsGoal> findByResidentId(Long residentId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT s FROM SavingsGoal s WHERE s.budget.resident.id = :residentId",
                            SavingsGoal.class)
                    .setParameter("residentId", residentId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error finding savings goals by resident id", e);
            throw new RuntimeException("Error finding savings goals by resident id", e);
        }
    }
}
