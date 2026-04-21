package com.carebridge.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateShiftRequestDTO {

    private LocalDateTime startShift;
    private LocalDateTime endShift;
    private String shiftType;
    private Long planPeriodId;
    private Long locationId;

    // PlanPeriod dates for validation
    private LocalDate planPeriodStart;
    private LocalDate planPeriodEnd;

    public LocalDateTime getStartShift() {
        return startShift;
    }

    public LocalDateTime getEndShift() {
        return endShift;
    }

    public String getShiftType() {
        return shiftType;
    }

    public Long getPlanPeriodId() {
        return planPeriodId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public LocalDate getPlanPeriodStart() {
        return planPeriodStart;
    }

    public LocalDate getPlanPeriodEnd() {
        return planPeriodEnd;
    }
}