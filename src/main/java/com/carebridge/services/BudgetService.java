package com.carebridge.services;

import com.carebridge.dao.impl.SavingsGoalDAO;
import com.carebridge.dtos.SavingsGoalResponseDTO;
import com.carebridge.dtos.UpdateSavingsGoalRequestDTO;
import com.carebridge.entities.SavingsGoal;

import java.util.List;

public class BudgetService {

    private final SavingsGoalDAO savingsGoalDAO;

    public BudgetService() {
        this.savingsGoalDAO = SavingsGoalDAO.getInstance();
    }

    public SavingsGoal updateSavingsGoal(Long goalId, UpdateSavingsGoalRequestDTO dto) {
        SavingsGoal goal = savingsGoalDAO.read(goalId);

        if (dto.getGoalName() != null && !dto.getGoalName().isBlank()) {
            goal.setGoalName(dto.getGoalName());
        }
        if (dto.getTargetAmount() != null) {
            goal.setTargetAmount(dto.getTargetAmount());
        }
        if (dto.getMonthlySavingAmount() != null) {
            goal.setMonthlySavingAmount(dto.getMonthlySavingAmount());
        }
        if (dto.getCurrentBalance() != null) {
            goal.setCurrentBalance(dto.getCurrentBalance());
        }

        return savingsGoalDAO.update(goalId, goal);
    }

    public List<SavingsGoal> getSavingsGoalsByResidentId(Long residentId) {
        return savingsGoalDAO.findByResidentId(residentId);
    }

    public SavingsGoalResponseDTO toResponseDTO(SavingsGoal goal) {
        return new SavingsGoalResponseDTO(
                goal.getId(),
                goal.getBudget() != null ? goal.getBudget().getId() : null,
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getMonthlySavingAmount(),
                goal.getCurrentBalance()
        );
    }
}
