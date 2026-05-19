package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Budget;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BudgetDAO implements IDAO<Budget, Long> {

    private static final Logger logger = LoggerFactory.getLogger(BudgetDAO.class);

    private static final EntityManagerFactory emf =
            HibernateConfig.getEntityManagerFactory();

    private static BudgetDAO instance;

    public static synchronized BudgetDAO getInstance() {
        if (instance == null) {
            instance = new BudgetDAO();
        }
        return instance;
    }

    @Override
    public Budget create(Budget budget) {

        if (budget == null) {
            throw new ApiRuntimeException(400, "Budget cannot be null");
        }

        if (budget.getIncome() == null) {
            throw new ApiRuntimeException(400, "Income is required");
        }

        if (budget.getFixedExpenses() == null) {
            throw new ApiRuntimeException(400, "Fixed expenses are required");
        }

        if (budget.getVariableExpenses() == null) {
            throw new ApiRuntimeException(400, "Variable expenses are required");
        }

        if (budget.getPocketMoneyAmount() == null) {
            throw new ApiRuntimeException(400, "Pocket money amount is required");
        }

        if (budget.getSavingsAmount() == null) {
            throw new ApiRuntimeException(400, "Savings amount is required");
        }

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            em.persist(budget);

            em.getTransaction().commit();

            return budget;

        } catch (Exception e) {

            logger.error("Error persisting budget", e);

            throw new ApiRuntimeException(
                    500,
                    "Error persisting budget: " + e.getMessage()
            );
        }
    }

    @Override
    public Budget read(Long id) {

        try (EntityManager em = emf.createEntityManager()) {

            Budget budget = em.find(Budget.class, id);

            if (budget == null) {
                throw new EntityNotFoundException(
                        "Budget not found with ID: " + id
                );
            }

            return budget;

        } catch (Exception e) {

            logger.error("Error retrieving budget", e);

            throw new RuntimeException(
                    "Error retrieving budget",
                    e
            );
        }
    }

    @Override
    public List<Budget> readAll() {

        try (EntityManager em = emf.createEntityManager()) {

            return em.createQuery(
                    "SELECT b FROM Budget b",
                    Budget.class
            ).getResultList();

        } catch (Exception e) {

            logger.error("Error retrieving budgets", e);

            throw new RuntimeException(
                    "Error retrieving budgets",
                    e
            );
        }
    }

    @Override
    public Budget update(Long id, Budget updatedBudget) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            Budget managed = em.find(Budget.class, id);

            if (managed == null) {

                em.getTransaction().rollback();

                throw new ApiRuntimeException(
                        404,
                        "Budget not found with ID: " + id
                );
            }

            managed.setIncome(updatedBudget.getIncome());
            managed.setFixedExpenses(updatedBudget.getFixedExpenses());
            managed.setVariableExpenses(updatedBudget.getVariableExpenses());
            managed.setPocketMoneyAmount(updatedBudget.getPocketMoneyAmount());
            managed.setSavingsAmount(updatedBudget.getSavingsAmount());
            managed.setNotes(updatedBudget.getNotes());

            em.merge(managed);

            em.getTransaction().commit();

            return managed;

        } catch (Exception e) {

            logger.error("Error updating budget", e);

            throw new ApiRuntimeException(
                    500,
                    "Error updating budget: " + e.getMessage()
            );
        }
    }

    @Override
    public void delete(Long id) {

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            Budget budget = em.find(Budget.class, id);

            if (budget == null) {

                em.getTransaction().rollback();

                throw new EntityNotFoundException(
                        "Budget not found with ID: " + id
                );
            }

            em.remove(budget);

            em.getTransaction().commit();

        } catch (Exception e) {

            logger.error("Error deleting budget", e);

            throw new RuntimeException(
                    "Error deleting budget",
                    e
            );
        }
    }
}