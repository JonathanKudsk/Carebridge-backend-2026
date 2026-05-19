package com.carebridge.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class BudgetResponseDTO {

    private Long id;
    private BigDecimal income;
    private BigDecimal fixedExpenses;
    private BigDecimal variableExpenses;
    private BigDecimal pocketMoneyAmount;
    private BigDecimal savingsAmount;
    private String notes;
    private Long residentId;

    public BudgetResponseDTO() {
    }

    public BudgetResponseDTO(
            Long id,
            BigDecimal income,
            BigDecimal fixedExpenses,
            BigDecimal variableExpenses,
            BigDecimal pocketMoneyAmount,
            BigDecimal savingsAmount,
            String notes,
            Long residentId
    ) {
        this.id = id;
        this.income = income;
        this.fixedExpenses = fixedExpenses;
        this.variableExpenses = variableExpenses;
        this.pocketMoneyAmount = pocketMoneyAmount;
        this.savingsAmount = savingsAmount;
        this.notes = notes;
        this.residentId = residentId;
    }
}