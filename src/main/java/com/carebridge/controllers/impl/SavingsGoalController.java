package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.SavingsGoalDAO;
import com.carebridge.dtos.CreateSavingsGoalRequestDTO;
import com.carebridge.dtos.SavingsGoalResponseDTO;
import com.carebridge.dtos.UpdateSavingsGoalRequestDTO;
import com.carebridge.entities.Budget;
import com.carebridge.entities.SavingsGoal;
import com.carebridge.services.BudgetService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SavingsGoalController implements IController<SavingsGoal, Long> {

    private static final Logger logger = LoggerFactory.getLogger(SavingsGoalController.class);
    private final SavingsGoalDAO savingsGoalDAO = SavingsGoalDAO.getInstance();
    private final BudgetService budgetService = new BudgetService();

    @Override
    public void create(Context ctx) {
        try {
            CreateSavingsGoalRequestDTO req = ctx.bodyAsClass(CreateSavingsGoalRequestDTO.class);

            if (req.getBudgetId() == null) throw new IllegalArgumentException("budgetId is required");
            if (req.getGoalName() == null || req.getGoalName().isBlank()) throw new IllegalArgumentException("goalName is required");
            if (req.getTargetAmount() <= 0) throw new IllegalArgumentException("targetAmount must be greater than 0");
            if (req.getMonthlySavingAmount() <= 0) throw new IllegalArgumentException("monthlySavingAmount must be greater than 0");
            if (req.getCurrentBalance() < 0) throw new IllegalArgumentException("currentBalance cannot be negative");

            // Budget reference — ID only, em.getReference is used inside the DAO
            Budget budgetRef = new Budget();
            budgetRef.setId(req.getBudgetId());

            SavingsGoal goal = new SavingsGoal();
            goal.setGoalName(req.getGoalName());
            goal.setTargetAmount(req.getTargetAmount());
            goal.setMonthlySavingAmount(req.getMonthlySavingAmount());
            goal.setCurrentBalance(req.getCurrentBalance());
            goal.setBudget(budgetRef);

            SavingsGoal created = savingsGoalDAO.create(goal);
            ctx.status(201).json(budgetService.toResponseDTO(created));
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create savings goal", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            UpdateSavingsGoalRequestDTO req = ctx.bodyAsClass(UpdateSavingsGoalRequestDTO.class);

            SavingsGoal updated = budgetService.updateSavingsGoal(id, req);
            ctx.status(200).json(budgetService.toResponseDTO(updated));
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update savings goal", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            SavingsGoal goal = savingsGoalDAO.read(id);
            ctx.status(200).json(budgetService.toResponseDTO(goal));
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to read savings goal", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            List<SavingsGoal> goals = savingsGoalDAO.readAll();
            List<SavingsGoalResponseDTO> dtos = goals.stream()
                    .map(budgetService::toResponseDTO)
                    .toList();
            ctx.status(200).json(dtos);
        } catch (Exception e) {
            logger.error("Failed to read all savings goals", e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void getByResident(Context ctx) {
        try {
            Long residentId = Long.parseLong(ctx.pathParam("residentId"));
            List<SavingsGoal> goals = budgetService.getSavingsGoalsByResidentId(residentId);
            List<SavingsGoalResponseDTO> dtos = goals.stream()
                    .map(budgetService::toResponseDTO)
                    .toList();
            ctx.status(200).json(dtos);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get savings goals for resident", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            savingsGoalDAO.delete(id);
            ctx.status(204);
        } catch (Exception e) {
            logger.error("Failed to delete savings goal", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) { return false; }

    @Override
    public SavingsGoal validateEntity(Context ctx) { return null; }
}
