package com.carebridge.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class CreateBudgetRequestDTO {

    private BigDecimal income;
    private BigDecimal fixedExpenses;
    private BigDecimal variableExpenses;
    private BigDecimal pocketMoneyAmount;
    private BigDecimal savingsAmount;
    private String notes;
    private Long residentId;

    public CreateBudgetRequestDTO() {
    }
}