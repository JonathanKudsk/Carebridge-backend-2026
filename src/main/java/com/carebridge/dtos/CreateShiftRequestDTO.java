package com.carebridge.dtos;

import com.carebridge.enums.ShiftType;
import java.time.LocalDateTime;

public class CreateShiftRequestDTO {
    private LocalDateTime startShift;
    private LocalDateTime endShift;
    private ShiftType shiftType;
    private Long planPeriodId;
    private Long locationId;

    public LocalDateTime getStartShift() { return startShift; }
    public void setStartShift(LocalDateTime startShift) { this.startShift = startShift; }

    public LocalDateTime getEndShift() { return endShift; }
    public void setEndShift(LocalDateTime endShift) { this.endShift = endShift; }

    public ShiftType getShiftType() { return shiftType; }
    public void setShiftType(ShiftType shiftType) { this.shiftType = shiftType; }

    public Long getPlanPeriodId() { return planPeriodId; }
    public void setPlanPeriodId(Long planPeriodId) { this.planPeriodId = planPeriodId; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
}