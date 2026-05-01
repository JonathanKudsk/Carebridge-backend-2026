package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ShiftAssignmentDTO {
    private Long id;
    private Long shiftId;
    private Long userId;
    private Instant assignedAt;
    private Long assignedBy;
}

