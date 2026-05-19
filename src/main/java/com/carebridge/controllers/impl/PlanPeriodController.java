package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.PlanPeriodDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.CreatePlanPeriodRequestDTO;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.PlanPeriodResponseDTO;
import com.carebridge.entities.PlanPeriod;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.PlanStatus;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import com.carebridge.utils.toon.ToonObjectMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class PlanPeriodController {

    private static final Logger logger = LoggerFactory.getLogger(PlanPeriodController.class);
    private final PlanPeriodDAO planPeriodDAO = PlanPeriodDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final ToonObjectMapper toonObjectMapper = new ToonObjectMapper();

    public void create(Context ctx) {
        try {
            CreatePlanPeriodRequestDTO dto = toonObjectMapper.readValue(ctx.body(), CreatePlanPeriodRequestDTO.class);

            if (dto.getStartDate() == null || dto.getEndDate() == null) {
                throw new ValidationException("startDate and endDate are required");
            }
            if (!dto.getEndDate().isAfter(dto.getStartDate())) {
                throw new ValidationException("endDate must be after startDate");
            }

            Long currentUserId = extractCurrentUserId(ctx);
            if (currentUserId == null) {
                throw new ApiRuntimeException(401, "Unauthorized");
            }

            PlanPeriod planPeriod = new PlanPeriod();
            planPeriod.setStartDate(dto.getStartDate());
            planPeriod.setEndDate(dto.getEndDate());
            planPeriod.setStatus(PlanStatus.DRAFT);
            planPeriod.setCreatedBy(currentUserId);
            planPeriod.setCreatedAt(Instant.now());

            PlanPeriod created = planPeriodDAO.create(planPeriod);
            toonObjectMapper.write(ctx, 201, toResponseDTO(created));
        } catch (ValidationException e) {
            toonObjectMapper.writeMessage(ctx, 400, e.getMessage());
        } catch (ApiRuntimeException e) {
            toonObjectMapper.writeMessage(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("create plan period failed", e);
            toonObjectMapper.writeMessage(ctx, 500, "Internal error");
        }
    }

    public void read(Context ctx) {
        try {
            Long id = parseId(ctx);
            PlanPeriod planPeriod = planPeriodDAO.read(id);
            if (planPeriod == null) {
                toonObjectMapper.writeMessage(ctx, 404, "Plan period not found");
                return;
            }

            toonObjectMapper.write(ctx, 200, toResponseDTO(planPeriod));
        } catch (ApiRuntimeException e) {
            toonObjectMapper.writeMessage(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("read plan period failed", e);
            toonObjectMapper.writeMessage(ctx, 500, "Internal error");
        }
    }

    public void readAll(Context ctx) {
        try {
            var response = planPeriodDAO.readAll().stream()
                    .map(this::toResponseDTO)
                    .toList();
            toonObjectMapper.write(ctx, 200, response);
        } catch (ApiRuntimeException e) {
            toonObjectMapper.writeMessage(ctx, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("readAll plan periods failed", e);
            toonObjectMapper.writeMessage(ctx, 500, "Internal error");
        }
    }

    private PlanPeriodResponseDTO toResponseDTO(PlanPeriod planPeriod) {
        return new PlanPeriodResponseDTO(
                planPeriod.getId(),
                planPeriod.getStartDate(),
                planPeriod.getEndDate(),
                planPeriod.getStatus(),
                planPeriod.getCreatedBy(),
                planPeriod.getCreatedAt()
        );
    }

    private Long extractCurrentUserId(Context ctx) {
        Object tokenUser = ctx.attribute("user");
        if (tokenUser instanceof JwtUserDTO jwtUserDTO) {
            User user = userDAO.readByEmail(jwtUserDTO.getUsername());
            return user != null ? user.getId() : null;
        }
        if (tokenUser instanceof com.carebridge.dtos.UserDTO userDTO) {
            return userDTO.getId();
        }
        return null;
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (Exception e) {
            throw new ApiRuntimeException(400, "Invalid id");
        }
    }
}
