package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSavingsGoalRequestDTO {
    private Long budgetId;
    private String goalName;
    private double targetAmount;
    private double monthlySavingAmount;
    private double currentBalance;
}
