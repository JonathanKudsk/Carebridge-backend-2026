package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.dtos.CreateShiftRequestDTO;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import com.carebridge.exceptions.PlanPeriodException;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftController {

    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);
    private final ShiftDAO shiftDAO = ShiftDAO.getInstance();

    public void create(Context ctx) {

        try {

            CreateShiftRequestDTO dto = ctx.bodyAsClass(CreateShiftRequestDTO.class);

            // Required fields
            if (dto.getStartShift() == null || dto.getEndShift() == null) {
                throw new ValidationException("startShift and endShift are required");
            }

            // Rule 1: endShift must be after startShift
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

            // Rule 2: Shift must be within PlanPeriod
            if (dto.getStartShift().toLocalDate().isBefore(dto.getPlanPeriodStart()) ||
                    dto.getEndShift().toLocalDate().isAfter(dto.getPlanPeriodEnd())) {

                throw new PlanPeriodException("Shift must be within PlanPeriod dates");
            }

            Long createdBy = ctx.attribute("userId");

            Shift shift = new Shift(
                    dto.getStartShift(),
                    dto.getEndShift(),
                    dto.getShiftType(),
                    dto.getPlanPeriodId(),
                    dto.getLocationId(),
                    createdBy
            );

            Shift created = shiftDAO.create(shift);

            ctx.status(201).json(created);
            logger.info("Shift created with id: {}", created.getId());

        }

        catch (ValidationException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            ctx.status(400).json("{\"msg\":\"" + e.getMessage() + "\"}");
        }

        catch (PlanPeriodException e) {
            logger.warn("PlanPeriod validation failed: {}", e.getMessage());
            ctx.status(400).json("{\"msg\":\"" + e.getMessage() + "\"}");
        }

        catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        }

        catch (Exception e) {
            logger.error("Error creating shift", e);
            ctx.status(500).json("{\"msg\":\"Internal server error\"}");
        }
    }
}