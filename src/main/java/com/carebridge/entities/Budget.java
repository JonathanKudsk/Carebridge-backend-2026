package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

@Entity
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal income;

    @Column(nullable = false)
    private BigDecimal fixedExpenses;

    @Column(nullable = false)
    private BigDecimal variableExpenses;

    @Column(nullable = false)
    private BigDecimal pocketMoneyAmount;

    @Column(nullable = false)
    private BigDecimal savingsAmount;

    @Column(length = 1000)
    private String notes;

    @OneToOne
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    public Budget() {
    }
}