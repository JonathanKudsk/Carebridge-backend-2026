package com.carebridge.dtos;

import com.carebridge.entities.enums.PlanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanPeriodResponseDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private PlanStatus status;
    private Long createdBy;
    private Instant createdAt;
}
