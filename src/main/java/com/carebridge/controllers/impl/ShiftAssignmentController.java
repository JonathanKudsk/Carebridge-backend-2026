package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.dtos.ShiftAssignmentDTO;
import com.carebridge.entities.Shift;
import com.carebridge.enums.ShiftStatus;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ScheduleConflictException;
import com.carebridge.exceptions.ValidationException;
import com.carebridge.utils.toon.ToonObjectMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

public class ShiftAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(ShiftAssignmentController.class);
    private final ShiftDAO shiftDAO = ShiftDAO.getInstance();
    private final ToonObjectMapper toonObjectMapper = new ToonObjectMapper();

    public void create(Context ctx) {
        try {
            ShiftAssignmentDTO dto = toonObjectMapper.readValue(ctx.body(), ShiftAssignmentDTO.class);

            if (dto.getShiftId() == null) throw new ValidationException("shiftId is required");
            if (dto.getUserId() == null) throw new ValidationException("userId is required");

            Shift shift = shiftDAO.read(dto.getShiftId());
            if (shift == null) {
                toonObjectMapper.writeMessage(ctx, 404, "Shift not found");
                return;
            }

            validateNoOverlap(dto.getUserId(), shift.getStartShift(), shift.getEndShift());

            Long assignedBy = ctx.attribute("userId");
            Object createdAssignment = createShiftAssignment(dto.getShiftId(), dto.getUserId(), assignedBy);

            Shift patch = new Shift();
            patch.setStatus(ShiftStatus.ASSIGNED);
            patch.setAssignedUserId(dto.getUserId());
            shiftDAO.update(dto.getShiftId(), patch);

            toonObjectMapper.write(ctx, 201, createdAssignment);
        } catch (ValidationException e) {
            toonObjectMapper.writeMessage(ctx, 400, e.getMessage());
        } catch (ScheduleConflictException e) {
            toonObjectMapper.writeMessage(ctx, 409, e.getMessage());
        } catch (ApiRuntimeException e) {
            toonObjectMapper.writeMessage(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating shift assignment", e);
            toonObjectMapper.writeMessage(ctx, 500, "Internal server error");
        }
    }

    private void validateNoOverlap(Long userId, LocalDateTime start, LocalDateTime end) {
        try {
            Class<?> cls = Class.forName("com.carebridge.services.ShiftService");
            Object service = cls.getDeclaredConstructor().newInstance();
            cls.getMethod("validateNoOverlap", Long.class, LocalDateTime.class, LocalDateTime.class)
                    .invoke(service, userId, start, end);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        } catch (ScheduleConflictException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("ShiftService.validateNoOverlap is not available", e);
        }
    }

    private Object createShiftAssignment(Long shiftId, Long userId, Long assignedBy) {
        try {
            Class<?> daoCls = Class.forName("com.carebridge.dao.impl.ShiftAssignmentDAO");
            Object dao = daoCls.getMethod("getInstance").invoke(null);

            Class<?> entityCls = Class.forName("com.carebridge.entities.ShiftAssignment");
            Object entity = entityCls.getDeclaredConstructor().newInstance();
            entityCls.getMethod("setShiftId", Long.class).invoke(entity, shiftId);
            entityCls.getMethod("setUserId", Long.class).invoke(entity, userId);
            if (assignedBy != null) {
                try {
                    entityCls.getMethod("setAssignedBy", Long.class).invoke(entity, assignedBy);
                } catch (NoSuchMethodException ignored) {
                    // optional field
                }
            }

            return daoCls.getMethod("create", entityCls).invoke(dao, entity);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new IllegalStateException("ShiftAssignmentDAO/ShiftAssignment entity is not available", e);
        }
    }

}
