package com.carebridge.dtos;

public class SavingsGoalResponseDTO {
    private Long id;
    private Long budgetId;
    private String goalName;
    private double targetAmount;
    private double monthlySavingAmount;
    private double currentBalance;
    private double progressPercentage;

    public SavingsGoalResponseDTO() {}

    public SavingsGoalResponseDTO(Long id, Long budgetId, String goalName,
                                   double targetAmount, double monthlySavingAmount, double currentBalance) {
        this.id = id;
        this.budgetId = budgetId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.monthlySavingAmount = monthlySavingAmount;
        this.currentBalance = currentBalance;
        this.progressPercentage = targetAmount > 0 ? (currentBalance / targetAmount) * 100.0 : 0.0;
    }

    public Long getId() { return id; }
    public Long getBudgetId() { return budgetId; }
    public String getGoalName() { return goalName; }
    public double getTargetAmount() { return targetAmount; }
    public double getMonthlySavingAmount() { return monthlySavingAmount; }
    public double getCurrentBalance() { return currentBalance; }
    public double getProgressPercentage() { return progressPercentage; }
}
