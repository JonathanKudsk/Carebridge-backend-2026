package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.AuditLogDAO;
import com.carebridge.dtos.AuditLogResponseDTO;
import com.carebridge.entities.AuditLog;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AuditLogController {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogController.class);
    private final AuditLogDAO auditLogDAO = AuditLogDAO.getInstance();

    public void readAll(Context ctx) {
        try {
            List<AuditLog> logs = auditLogDAO.readAll();
            ctx.json(logs.stream().map(this::toResponseDTO).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Failed to get audit logs", e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void readByMedication(Context ctx) {
        try {
            Long medicationId = Long.parseLong(ctx.pathParam("medicationId"));
            List<AuditLog> logs = auditLogDAO.readByMedicationId(medicationId);
            ctx.json(logs.stream().map(this::toResponseDTO).collect(Collectors.toList()));
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid medication ID");
        } catch (Exception e) {
            logger.error("Failed to get audit logs for medication", e);
            ctx.status(500).result("Internal server error");
        }
    }

    private AuditLogResponseDTO toResponseDTO(AuditLog log) {
        return new AuditLogResponseDTO(
                log.getId(), log.getTimestamp(), log.getDoctorId(),
                log.getMedicationId(), log.getChartId(), log.getAction(), log.getDescription());
    }
}
