package com.carebridge.entities;

import jakarta.persistence.*;

@Entity
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String goalName;

    @Column(nullable = false)
    private double targetAmount;

    @Column(nullable = false)
    private double monthlySavingAmount;

    @Column(nullable = false)
    private double currentBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    public SavingsGoal() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getMonthlySavingAmount() { return monthlySavingAmount; }
    public void setMonthlySavingAmount(double monthlySavingAmount) { this.monthlySavingAmount = monthlySavingAmount; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

    public Budget getBudget() { return budget; }
    public void setBudget(Budget budget) { this.budget = budget; }
}
