package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.DosageDAO;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dtos.CreateDosageRequestDTO;
import com.carebridge.dtos.DosageResponseDTO;
import com.carebridge.dtos.UpdateDosageRequestDTO;
import com.carebridge.entities.Dosage;
import com.carebridge.entities.Resident;
import com.carebridge.services.mappers.DosageMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class DosageController {

    private static final Logger logger = LoggerFactory.getLogger(DosageController.class);
    private final DosageDAO dosageDAO = DosageDAO.getInstance();
    private final ResidentDAO residentDAO = ResidentDAO.getInstance();

    public DosageController() {}

    // POST /api/dosages
    public void create(Context ctx) {
        try {
            CreateDosageRequestDTO req = ctx.bodyAsClass(CreateDosageRequestDTO.class);

            if (req.getMedicineName() == null || req.getMedicineName().isBlank())
                throw new IllegalArgumentException("medicineName is required");
            if (req.getDosage() == null || req.getDosage().isBlank())
                throw new IllegalArgumentException("dosage is required");
            if (req.getFrequency() == null || req.getFrequency().isBlank())
                throw new IllegalArgumentException("frequency is required");
            if (req.getStartDate() == null)
                throw new IllegalArgumentException("startDate is required");
            if (req.getResidentId() == null)
                throw new IllegalArgumentException("residentId is required");

            Resident resident = residentDAO.read(req.getResidentId());

            Dosage dosage = new Dosage();
            dosage.setMedicineName(req.getMedicineName());
            dosage.setDosage(req.getDosage());
            dosage.setFrequency(req.getFrequency());
            dosage.setStartDate(req.getStartDate());
            dosage.setEndDate(req.getEndDate());
            dosage.setNote(req.getNote());
            dosage.setResident(resident);

            Dosage created = dosageDAO.create(dosage);
            DosageResponseDTO resp = DosageMapper.toDTO(created);

            ctx.status(201).json(resp);

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create dosage", e);
            ctx.status(500).result("Internal server error");
        }
    }

    // PUT /api/dosages/{id}
    public void update(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            UpdateDosageRequestDTO req = ctx.bodyAsClass(UpdateDosageRequestDTO.class);

            // hent doctor ID fra token
            Long doctorId = null;
            var tokenUser = ctx.attribute("user");
            if (tokenUser instanceof com.carebridge.dtos.UserDTO du) {
                doctorId = du.getId();
            }

            Dosage updated = new Dosage();
            updated.setMedicineName(req.getMedicineName());
            updated.setDosage(req.getDosage());
            updated.setFrequency(req.getFrequency());
            updated.setStartDate(req.getStartDate());
            updated.setEndDate(req.getEndDate());
            updated.setNote(req.getNote());
            updated.setUpdatedAt(LocalDateTime.now());
            updated.setUpdatedByUserId(doctorId);

            Dosage result = dosageDAO.update(id, updated);
            DosageResponseDTO resp = DosageMapper.toDTO(result);

            ctx.status(200).json(resp);

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update dosage", e);
            ctx.status(500).result("Internal server error");
        }
    }

    // DELETE /api/dosages/{id}
    public void delete(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            dosageDAO.delete(id);
            ctx.status(204);

        } catch (Exception e) {
            logger.error("Failed to delete dosage", e);
            ctx.status(500).result("Internal server error");
        }
    }

    // GET /api/residents/{residentId}/dosages
    public void readAllByResident(Context ctx) {
        try {
            Long residentId = Long.parseLong(ctx.pathParam("residentId"));
            List<Dosage> dosages = dosageDAO.readAllByResident(residentId);
            List<DosageResponseDTO> resp = dosages.stream()
                    .map(DosageMapper::toDTO)
                    .toList();

            ctx.status(200).json(resp);

        } catch (Exception e) {
            logger.error("Failed to retrieve dosages", e);
            ctx.status(500).result("Internal server error");
        }
    }
}