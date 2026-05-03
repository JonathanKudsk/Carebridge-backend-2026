package com.carebridge.services.mappers;

import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ScheduleConflictException;

import java.time.LocalDateTime;
import java.util.List;

public class ShiftService {

    private static ShiftService instance;
    private final ShiftDAO shiftDAO = ShiftDAO.getInstance();

    private ShiftService() {}

    public static synchronized ShiftService getInstance() {
        if (instance == null) instance = new ShiftService();
        return instance;
    }

    /**
     * Validates that a new shift does not overlap with any existing shifts for the given user.
     * Overlap rule: A.startShift < B.endShift AND A.endShift > B.startShift
     *
     * @param userId     the ID of the employee being scheduled
     * @param startShift the proposed shift start time
     * @param endShift   the proposed shift end time
     * @throws ScheduleConflictException if an overlapping shift is found
     */
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

    /**
     * Validates that updating an existing shift does not cause overlap with other shifts for the given user.
     * Excludes the shift being updated (by shiftId) from the overlap check.
     * Overlap rule: A.startShift < B.endShift AND A.endShift > B.startShift
     *
     * @param shiftId    the ID of the shift being updated (excluded from check)
     * @param userId     the ID of the employee being scheduled
     * @param startShift the proposed new shift start time
     * @param endShift   the proposed new shift end time
     * @throws ScheduleConflictException if an overlapping shift is found
     */
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