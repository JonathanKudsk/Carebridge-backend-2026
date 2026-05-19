package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanPeriodRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
