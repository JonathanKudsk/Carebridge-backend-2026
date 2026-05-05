package com.carebridge.services.mappers;

import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ScheduleConflictException;

import java.time.LocalDateTime;
import java.util.List;

public class ShiftService {

    private static ShiftService instance;
    private ShiftDAO shiftDAO;

    private ShiftService() {
        this.shiftDAO = ShiftDAO.getInstance();
    }

    public ShiftService(ShiftDAO shiftDAO) {
        this.shiftDAO = shiftDAO;
    }

    public static synchronized ShiftService getInstance() {
        if (instance == null) instance = new ShiftService();
        return instance;
    }

    public void validateNoOverlap(Long userId, LocalDateTime startShift, LocalDateTime endShift) {
        List<Shift> existingShifts = shiftDAO.findByAssignedUserId(userId);

        boolean hasOverlap = existingShifts.stream()
                .anyMatch(existing ->
                        startShift.isBefore(existing.getEndShift()) &&
                                endShift.isAfter(existing.getStartShift())
                );

        if (hasOverlap) {
            throw new ScheduleConflictException("Medarbejderen er allerede planlagt i dette tidsrum");
        }
    }

    public void validateNoOverlapOnUpdate(Long shiftId, Long userId, LocalDateTime startShift, LocalDateTime endShift) {
        List<Shift> existingShifts = shiftDAO.findByAssignedUserId(userId);

        boolean hasOverlap = existingShifts.stream()
                .filter(existing -> !existing.getId().equals(shiftId))
                .anyMatch(existing ->
                        startShift.isBefore(existing.getEndShift()) &&
                                endShift.isAfter(existing.getStartShift())
                );

        if (hasOverlap) {
            throw new ScheduleConflictException("Medarbejderen er allerede planlagt i dette tidsrum");
        }
    }
}
