package com.carebridge.services;

import com.carebridge.dao.impl.BudgetDAO;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.SavingsGoalDAO;
import com.carebridge.dtos.BudgetResponseDTO;
import com.carebridge.dtos.CreateBudgetRequestDTO;
import com.carebridge.dtos.SavingsGoalResponseDTO;
import com.carebridge.dtos.UpdateSavingsGoalRequestDTO;
import com.carebridge.entities.Budget;
import com.carebridge.entities.Resident;
import com.carebridge.entities.SavingsGoal;
import com.carebridge.exceptions.ApiRuntimeException;

import java.util.List;

public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final ResidentDAO residentDAO;
    private final SavingsGoalDAO savingsGoalDAO;

    public BudgetService() {
        this.budgetDAO = BudgetDAO.getInstance();
        this.residentDAO = ResidentDAO.getInstance();
        this.savingsGoalDAO = SavingsGoalDAO.getInstance();
    }

    public BudgetResponseDTO createBudget(CreateBudgetRequestDTO request) {

        if (request == null) {
            throw new ApiRuntimeException(400, "Request cannot be null");
        }

        if (request.getResidentId() == null) {
            throw new ApiRuntimeException(400, "Resident id is required");
        }

        Resident resident = residentDAO.read(request.getResidentId());

        if (resident == null) {
            throw new ApiRuntimeException(404, "Resident not found");
        }

        if (resident.getBudget() != null) {
            throw new ApiRuntimeException(
                    400,
                    "Resident already has a budget"
            );
        }

        Budget budget = new Budget();

        budget.setIncome(request.getIncome());
        budget.setFixedExpenses(request.getFixedExpenses());
        budget.setVariableExpenses(request.getVariableExpenses());
        budget.setPocketMoneyAmount(request.getPocketMoneyAmount());
        budget.setSavingsAmount(request.getSavingsAmount());
        budget.setNotes(request.getNotes());

        budget.setResident(resident);

        resident.setBudget(budget);

        Budget createdBudget = budgetDAO.create(budget);

        return new BudgetResponseDTO(
                createdBudget.getId(),
                createdBudget.getIncome(),
                createdBudget.getFixedExpenses(),
                createdBudget.getVariableExpenses(),
                createdBudget.getPocketMoneyAmount(),
                createdBudget.getSavingsAmount(),
                createdBudget.getNotes(),
                resident.getId()
        );
    }

    public BudgetResponseDTO getBudgetByResident(Long residentId) {

        Resident resident = residentDAO.read(residentId);

        if (resident == null) {
            throw new ApiRuntimeException(
                    404,
                    "Resident not found"
            );
        }

        Budget budget = resident.getBudget();

        if (budget == null) {
            throw new ApiRuntimeException(
                    404,
                    "Resident has no budget"
            );
        }

        return new BudgetResponseDTO(
                budget.getId(),
                budget.getIncome(),
                budget.getFixedExpenses(),
                budget.getVariableExpenses(),
                budget.getPocketMoneyAmount(),
                budget.getSavingsAmount(),
                budget.getNotes(),
                resident.getId()
        );
    }

    public SavingsGoalResponseDTO toResponseDTO(SavingsGoal goal) {
        Long budgetId = goal.getBudget() != null ? goal.getBudget().getId() : null;
        return new SavingsGoalResponseDTO(
                goal.getId(),
                budgetId,
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getMonthlySavingAmount(),
                goal.getCurrentBalance()
        );
    }

    public SavingsGoal updateSavingsGoal(Long id, UpdateSavingsGoalRequestDTO req) {
        SavingsGoal goal = savingsGoalDAO.read(id);
        if (req.getGoalName() != null && !req.getGoalName().isBlank())
            goal.setGoalName(req.getGoalName());
        if (req.getTargetAmount() != null && req.getTargetAmount() > 0)
            goal.setTargetAmount(req.getTargetAmount());
        if (req.getMonthlySavingAmount() != null && req.getMonthlySavingAmount() > 0)
            goal.setMonthlySavingAmount(req.getMonthlySavingAmount());
        if (req.getCurrentBalance() != null && req.getCurrentBalance() >= 0)
            goal.setCurrentBalance(req.getCurrentBalance());
        return savingsGoalDAO.update(id, goal);
    }

    public List<SavingsGoal> getSavingsGoalsByResidentId(Long residentId) {
        return savingsGoalDAO.findByResidentId(residentId);
    }
}