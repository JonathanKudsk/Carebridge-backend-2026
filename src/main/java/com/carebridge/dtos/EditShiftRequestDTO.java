package com.carebridge.dtos;

import com.carebridge.enums.ShiftStatus;
import com.carebridge.enums.ShiftType;

import java.time.LocalDateTime;

public class EditShiftRequestDTO {
    private LocalDateTime startShift;
    private LocalDateTime endShift;
    private ShiftType shiftType;
    private ShiftStatus status;
    private Long locationId;
    private Long assignedUserId;

    public EditShiftRequestDTO() {
    }

    public LocalDateTime getStartShift() {
        return startShift;
    }

    public void setStartShift(LocalDateTime startShift) {
        this.startShift = startShift;
    }

    public LocalDateTime getEndShift() {
        return endShift;
    }

    public void setEndShift(LocalDateTime endShift) {
        this.endShift = endShift;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }
}

