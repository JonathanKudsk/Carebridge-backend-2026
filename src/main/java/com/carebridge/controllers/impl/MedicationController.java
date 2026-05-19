package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.AuditLogDAO;
import com.carebridge.dao.impl.MedicationChartDAO;
import com.carebridge.dao.impl.MedicationDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.CreateMedicationRequestDTO;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.MedicationChartResponseDTO;
import com.carebridge.dtos.MedicationResponseDTO;
import com.carebridge.dtos.UserDTO;
import com.carebridge.entities.AuditLog;
import com.carebridge.entities.Medication;
import com.carebridge.entities.MedicationChart;
import com.carebridge.entities.User;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class MedicationController implements IController<Medication, Long> {

    private static final Logger logger = LoggerFactory.getLogger(MedicationController.class);
    private final MedicationChartDAO medicationChartDAO = MedicationChartDAO.getInstance();
    private final MedicationDAO medicationDAO = MedicationDAO.getInstance();
    private final AuditLogDAO auditLogDAO = AuditLogDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();

    // GET /medication-charts/{chartId}
    @Override
    public void readAll(Context ctx) {
        try {
            Long chartId = Long.parseLong(ctx.pathParam("chartId"));
            MedicationChart chart = medicationChartDAO.read(chartId);
            ctx.json(toChartResponseDTO(chart));
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get medication chart", e);
            ctx.status(500).result("Internal server error");
        }
    }

    // GET /medication-charts/{chartId}/medications/{medicationId}
    @Override
    public void read(Context ctx) {
        try {
            Long chartId = Long.parseLong(ctx.pathParam("chartId"));
            Long medicationId = Long.parseLong(ctx.pathParam("medicationId"));
            Medication medication = medicationDAO.read(medicationId);
            if (!medication.getMedicationChart().getId().equals(chartId)) {
                throw new IllegalArgumentException(
                        "Medication " + medicationId + " does not belong to chart " + chartId);
            }
            ctx.json(toResponseDTO(medication));
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get medication", e);
            ctx.status(500).result("Internal server error");
        }
    }

    // POST /medication-charts/{chartId}/medications
    @Override
    public void create(Context ctx) {
        try {
            Long chartId = Long.parseLong(ctx.pathParam("chartId"));
            CreateMedicationRequestDTO req = ctx.bodyAsClass(CreateMedicationRequestDTO.class);

            if (req.getName() == null || req.getName().isBlank()) {
                throw new IllegalArgumentException("name is required");
            }

            Medication medication = new Medication();
            medication.setName(req.getName());
            medication.setDosage(req.getDosage());
            medication.setFrequency(req.getFrequency());
            medication.setStartDate(req.getStartDate());
            medication.setEndDate(req.getEndDate());
            medication.setPrescribingDoctor(req.getPrescribingDoctor());
            medication.setNotes(req.getNotes());
            medication.setActive(req.getActive() != null ? req.getActive() : true);

            Medication created = medicationChartDAO.addMedication(chartId, medication);
            ctx.status(201).json(toResponseDTO(created));
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to add medication", e);
            ctx.status(500).result("Internal server error");
        }
    }

    // PUT /medication-charts/{chartId}/medications/{medicationId}
    @Override
    public void update(Context ctx) {
        try {
            Long chartId = Long.parseLong(ctx.pathParam("chartId"));
            Long medicationId = Long.parseLong(ctx.pathParam("medicationId"));
            CreateMedicationRequestDTO req = ctx.bodyAsClass(CreateMedicationRequestDTO.class);

            Medication existing = medicationDAO.read(medicationId);
            if (!existing.getMedicationChart().getId().equals(chartId)) {
                throw new IllegalArgumentException(
                        "Medication " + medicationId + " does not belong to chart " + chartId);
            }

            if (req.getName() != null && !req.getName().isBlank()) existing.setName(req.getName());
            if (req.getDosage() != null) existing.setDosage(req.getDosage());
            if (req.getFrequency() != null) existing.setFrequency(req.getFrequency());
            if (req.getStartDate() != null) existing.setStartDate(req.getStartDate());
            if (req.getEndDate() != null) existing.setEndDate(req.getEndDate());
            if (req.getPrescribingDoctor() != null) existing.setPrescribingDoctor(req.getPrescribingDoctor());
            if (req.getNotes() != null) existing.setNotes(req.getNotes());
            if (req.getActive() != null) existing.setActive(req.getActive());

            Medication updated = medicationDAO.update(medicationId, existing);

            Long doctorId = extractDoctorId(ctx);
            auditLogDAO.create(new AuditLog(doctorId, medicationId, chartId,
                    "UPDATE_MEDICATION",
                    "Medication '" + updated.getName() + "' updated on chart " + chartId));

            ctx.status(200).json(toResponseDTO(updated));
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update medication", e);
            ctx.status(500).result("Internal server error");
        }
    }

    private Long extractDoctorId(Context ctx) {
        try {
            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof JwtUserDTO ju) email = ju.getUsername();
            else if (tokenUser instanceof UserDTO du) email = du.getEmail();
            if (email != null) {
                User user = userDAO.readByEmail(email);
                if (user != null) return user.getId();
            }
        } catch (Exception e) {
            logger.warn("Could not extract doctor ID for audit log", e);
        }
        return null;
    }

    // DELETE /medication-charts/{chartId}/medications/{medicationId}
    @Override
    public void delete(Context ctx) {
        try {
            Long chartId = Long.parseLong(ctx.pathParam("chartId"));
            Long medicationId = Long.parseLong(ctx.pathParam("medicationId"));
            medicationChartDAO.removeMedication(chartId, medicationId);
            ctx.status(204);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to delete medication", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) { return id != null && id > 0; }

    @Override
    public Medication validateEntity(Context ctx) { return null; }

    private MedicationResponseDTO toResponseDTO(Medication m) {
        Long chartId = m.getMedicationChart() != null ? m.getMedicationChart().getId() : null;
        return new MedicationResponseDTO(
                m.getId(), chartId, m.getName(), m.getDosage(), m.getFrequency(),
                m.getStartDate(), m.getEndDate(), m.getPrescribingDoctor(), m.getNotes(), m.isActive());
    }

    private MedicationChartResponseDTO toChartResponseDTO(MedicationChart chart) {
        Long residentId = chart.getResident() != null ? chart.getResident().getId() : null;
        List<MedicationResponseDTO> medications = chart.getMedications().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return new MedicationChartResponseDTO(chart.getId(), residentId, medications);
    }
}
