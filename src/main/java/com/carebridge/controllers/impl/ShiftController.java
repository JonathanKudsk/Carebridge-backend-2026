package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.PlanPeriodDAO;
import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.dtos.CreateShiftRequestDTO;
import com.carebridge.entities.PlanPeriod;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.PlanPeriodException;
import com.carebridge.exceptions.ValidationException;
import com.carebridge.utils.toon.ToonObjectMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftController {

    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);
    private final ShiftDAO shiftDAO = ShiftDAO.getInstance();
    private final PlanPeriodDAO planPeriodDAO = PlanPeriodDAO.getInstance();
    private final ToonObjectMapper toonObjectMapper = new ToonObjectMapper();

    public void create(Context ctx) {
        try {
            CreateShiftRequestDTO dto = toonObjectMapper.readValue(ctx.body(), CreateShiftRequestDTO.class);
            validateCreateRequest(dto);
            validateWithinPlanPeriod(dto.getPlanPeriodId(), dto.getStartShift(), dto.getEndShift());

            Long createdBy = ctx.attribute("userId");

            Shift shift = new Shift();
            shift.setStartShift(dto.getStartShift());
            shift.setEndShift(dto.getEndShift());
            shift.setShiftType(dto.getShiftType().name());
            shift.setLocation(dto.getLocationId().toString());
            shift.setStatus("OPEN");
            shift.setPlanPeriodId(dto.getPlanPeriodId());
            shift.setAssignedUserId(null);
            shift.setCreatedBy(createdBy);

            Shift created = shiftDAO.create(shift);
            toonObjectMapper.write(ctx, 201, created);
            logger.info("Shift created with id: {}", created.getId());

        } catch (ValidationException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            toonObjectMapper.writeMessage(ctx, 400, e.getMessage());
        } catch (PlanPeriodException e) {
            logger.warn("PlanPeriod validation failed: {}", e.getMessage());
            toonObjectMapper.writeMessage(ctx, 400, e.getMessage());
        } catch (ApiRuntimeException e) {
            toonObjectMapper.writeMessage(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating shift", e);
            toonObjectMapper.writeMessage(ctx, 500, "Internal server error");
        }
    }

    public void update(Context ctx) {
        try {
            Long id = parseId(ctx);
            CreateShiftRequestDTO dto = toonObjectMapper.readValue(ctx.body(), CreateShiftRequestDTO.class);

            Shift existing = shiftDAO.read(id);
            if (existing == null) {
                toonObjectMapper.writeMessage(ctx, 404, "Shift not found");
                return;
            }

            Shift merged = buildMergedShift(existing, dto);
            validateMergedShift(merged, dto);
            validateWithinPlanPeriod(merged.getPlanPeriodId(), merged.getStartShift(), merged.getEndShift());

            Shift patch = new Shift();
            patch.setStartShift(dto.getStartShift());
            patch.setEndShift(dto.getEndShift());
            if (dto.getShiftType() != null) {
                patch.setShiftType(dto.getShiftType().name());
            }
            if (dto.getLocationId() != null) {
                patch.setLocation(dto.getLocationId().toString());
            }
            patch.setPlanPeriodId(dto.getPlanPeriodId());

            Shift updated = shiftDAO.update(id, patch);
            toonObjectMapper.write(ctx, 200, updated);
            logger.info("Shift updated with id: {}", updated.getId());

        } catch (ValidationException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            toonObjectMapper.writeMessage(ctx, 400, e.getMessage());
        } catch (PlanPeriodException e) {
            logger.warn("PlanPeriod validation failed: {}", e.getMessage());
            toonObjectMapper.writeMessage(ctx, 400, e.getMessage());
        } catch (ApiRuntimeException e) {
            toonObjectMapper.writeMessage(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating shift", e);
            toonObjectMapper.writeMessage(ctx, 500, "Internal server error");
        }
    }

    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx);
            shiftDAO.delete(id);
            ctx.status(204);
            logger.info("Shift deleted with id: {}", id);
        } catch (ApiRuntimeException e) {
            if (e.getErrorCode() == 404) {
                toonObjectMapper.writeMessage(ctx, 404, "Vagt ikke fundet");
                return;
            }
            toonObjectMapper.writeMessage(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting shift", e);
            toonObjectMapper.writeMessage(ctx, 500, "Internal server error");
        }
    }

    private void validateCreateRequest(CreateShiftRequestDTO dto) throws ValidationException {
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
    }

    private void validateMergedShift(Shift merged, CreateShiftRequestDTO dto) throws ValidationException {
        if (merged.getStartShift() == null || merged.getEndShift() == null) {
            throw new ValidationException("startShift and endShift are required");
        }
        if (!merged.getEndShift().isAfter(merged.getStartShift())) {
            throw new ValidationException("endShift must be after startShift");
        }
        if ((dto.getShiftType() != null && dto.getShiftType().name().isBlank())
                || merged.getShiftType() == null || merged.getShiftType().isBlank()) {
            throw new ValidationException("shiftType is required");
        }
        if (merged.getPlanPeriodId() == null) {
            throw new ValidationException("planPeriodId is required");
        }
        if (merged.getLocation() == null || merged.getLocation().isBlank()) {
            throw new ValidationException("locationId is required");
        }
    }

    private void validateWithinPlanPeriod(Long planPeriodId, java.time.LocalDateTime startShift, java.time.LocalDateTime endShift) {
        PlanPeriod planPeriod = planPeriodDAO.read(planPeriodId);
        if (planPeriod == null) {
            throw new ApiRuntimeException(404, "Plan period not found");
        }
        if (startShift.toLocalDate().isBefore(planPeriod.getStartDate())
                || endShift.toLocalDate().isAfter(planPeriod.getEndDate())) {
            throw new PlanPeriodException("Shift must be within PlanPeriod dates");
        }
    }

    private Shift buildMergedShift(Shift existing, CreateShiftRequestDTO dto) {
        Shift merged = new Shift();
        merged.setStartShift(dto.getStartShift() != null ? dto.getStartShift() : existing.getStartShift());
        merged.setEndShift(dto.getEndShift() != null ? dto.getEndShift() : existing.getEndShift());
        merged.setShiftType(dto.getShiftType() != null ? dto.getShiftType().name() : existing.getShiftType());
        merged.setLocation(dto.getLocationId() != null ? dto.getLocationId().toString() : existing.getLocation());
        merged.setPlanPeriodId(dto.getPlanPeriodId() != null ? dto.getPlanPeriodId() : existing.getPlanPeriodId());
        return merged;
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (Exception e) {
            throw new ApiRuntimeException(400, "Invalid id");
        }
    }
}
