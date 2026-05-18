package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.dtos.CreateShiftRequestDTO;
import com.carebridge.dtos.EditShiftRequestDTO;
import com.carebridge.entities.Shift;
import com.carebridge.enums.ShiftStatus;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.PlanPeriodException;
import com.carebridge.exceptions.ScheduleConflictException;
import com.carebridge.exceptions.ValidationException;
import com.carebridge.services.mappers.ShiftService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftController {

    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);
    private static final String TOON_CONTENT_TYPE = "application/toon";

    private final ShiftDAO shiftDAO = ShiftDAO.getInstance();
    private final ShiftService shiftService = ShiftService.getInstance();

    public void create(Context ctx) {
        try {
            CreateShiftRequestDTO dto = ctx.bodyAsClass(CreateShiftRequestDTO.class);

            if (dto.getStartShift() == null || dto.getEndShift() == null) {
                throw new ValidationException("startShift and endShift are required");
            }

            if (!dto.getEndShift().isAfter(dto.getStartShift())) {
                throw new ValidationException("endShift must be after startShift");
            }

            if (dto.getShiftType() == null) {
                throw new ValidationException("shiftType is required");
            }

            if (dto.getPlanPeriodId() == null) {
                throw new ValidationException("planPeriodId is required");
            }

            if (dto.getLocationId() == null) {
                throw new ValidationException("locationId is required");
            }

            // TODO: validate against PlanPeriod dates once PlanPeriodDAO is merged
            // PlanPeriod planPeriod = planPeriodDAO.read(dto.getPlanPeriodId());
            // if (dto.getStartShift().toLocalDate().isBefore(planPeriod.getStartDate()) ||
            //     dto.getEndShift().toLocalDate().isAfter(planPeriod.getEndDate())) {
            //     throw new PlanPeriodException("Shift must be within PlanPeriod dates");
            // }

            Long createdBy = ctx.attribute("userId");

            Shift shift = new Shift();
            shift.setStartShift(dto.getStartShift());
            shift.setEndShift(dto.getEndShift());
            shift.setShiftType(dto.getShiftType().name());
            shift.setLocation(dto.getLocationId().toString());
            shift.setStatus(ShiftStatus.OPEN);
            shift.setPlanPeriodId(dto.getPlanPeriodId());
            shift.setAssignedUserId(null);
            shift.setCreatedBy(createdBy);

            Shift created = shiftDAO.create(shift);
            ctx.status(201).contentType(TOON_CONTENT_TYPE).json(created);
            logger.info("Shift created with id: {}", created.getId());

        } catch (ValidationException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            respondError(ctx, 400, e.getMessage());
        } catch (PlanPeriodException e) {
            logger.warn("PlanPeriod validation failed: {}", e.getMessage());
            respondError(ctx, 400, e.getMessage());
        } catch (ApiRuntimeException e) {
            respondError(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating shift", e);
            respondError(ctx, 500, "Internal server error");
        }
    }

    public void update(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            EditShiftRequestDTO dto = ctx.bodyAsClass(EditShiftRequestDTO.class);

            if (dto.getStartShift() == null || dto.getEndShift() == null) {
                throw new ValidationException("startShift and endShift are required");
            }

            if (!dto.getEndShift().isAfter(dto.getStartShift())) {
                throw new ValidationException("endShift must be after startShift");
            }

            // Load existing shift to access its PlanPeriod
            Shift existing = shiftDAO.read(id);

            // TODO: validate against PlanPeriod dates once PlanPeriodDAO is merged
            // PlanPeriod planPeriod = planPeriodDAO.read(existing.getPlanPeriodId());
            // if (dto.getStartShift().toLocalDate().isBefore(planPeriod.getStartDate()) ||
            //     dto.getEndShift().toLocalDate().isAfter(planPeriod.getEndDate())) {
            //     throw new PlanPeriodException("Shift must be within PlanPeriod dates");
            // }

            if (dto.getAssignedUserId() != null) {
                shiftService.validateNoOverlapOnUpdate(
                        id,
                        dto.getAssignedUserId(),
                        dto.getStartShift(),
                        dto.getEndShift()
                );
            }

            Shift shift = new Shift();
            shift.setStartShift(dto.getStartShift());
            shift.setEndShift(dto.getEndShift());
            if (dto.getShiftType() != null) {
                shift.setShiftType(dto.getShiftType().name());
            }
            if (dto.getLocationId() != null) {
                shift.setLocation(dto.getLocationId().toString());
            }
            shift.setAssignedUserId(dto.getAssignedUserId());
            shift.setPlanPeriodId(existing.getPlanPeriodId());

            Shift updated = shiftDAO.update(id, shift);
            ctx.status(200).contentType(TOON_CONTENT_TYPE).json(updated);
            logger.info("Shift updated with id: {}", id);

        } catch (NumberFormatException e) {
            logger.warn("Invalid shift id path parameter");
            respondError(ctx, 400, "Invalid shift id");
        } catch (ValidationException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            respondError(ctx, 400, e.getMessage());
        } catch (PlanPeriodException e) {
            logger.warn("PlanPeriod validation failed: {}", e.getMessage());
            respondError(ctx, 400, e.getMessage());
        } catch (ScheduleConflictException e) {
            logger.warn("Schedule conflict: {}", e.getMessage());
            respondError(ctx, 409, e.getMessage());
        } catch (ApiRuntimeException e) {
            respondError(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating shift", e);
            respondError(ctx, 500, "Internal server error");
        }
    }

    private void respondError(Context ctx, int status, String message) {
        ctx.status(status)
                .contentType(TOON_CONTENT_TYPE)
                .result("msg: " + message);
    }
}
