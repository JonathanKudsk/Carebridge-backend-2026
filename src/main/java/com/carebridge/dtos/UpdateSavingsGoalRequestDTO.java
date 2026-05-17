package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSavingsGoalRequestDTO {
    private String goalName;
    private Double targetAmount;
    private Double monthlySavingAmount;
    private Double currentBalance;
}
