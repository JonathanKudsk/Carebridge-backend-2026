package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.CreateResidentRequestDTO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.MedicationChart;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.services.ResidentService;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ResidentController implements IController<Resident, Long> {

    private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);
    private final ResidentDAO residentDAO = ResidentDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final ResidentService residentService = new ResidentService();

    public void create(Context ctx) {
        try {
            CreateResidentRequestDTO req = ctx.bodyAsClass(CreateResidentRequestDTO.class);

            if (req == null) {
                throw new IllegalArgumentException("Request body is required");
            }
            if (req.getFirstName() == null || req.getFirstName().isBlank()) {
                throw new IllegalArgumentException("firstName is required");
            }
            if (req.getLastName() == null || req.getLastName().isBlank()) {
                throw new IllegalArgumentException("lastName is required");
            }

            Resident resident = new Resident();
            resident.setFirstName(req.getFirstName());
            resident.setLastName(req.getLastName());
            resident.setCprNr(req.getCprNr());

            Journal journal = new Journal();
            resident.setJournal(journal);
            journal.setResident(resident);

            MedicationChart medicationChart = new MedicationChart();
            medicationChart.setResident(resident);
            resident.setMedicationChart(medicationChart);

            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof com.carebridge.dtos.JwtUserDTO ju) {
                email = ju.getUsername();
            } else if (tokenUser instanceof com.carebridge.dtos.UserDTO du) {
                email = du.getEmail();
            } else if (tokenUser != null) {
                email = tokenUser.toString();
            }

            if (email != null) {
                User user = userDAO.readByEmail(email);
                if (user != null) {
                    resident.addUser(user);
                }
            }

            Resident created = residentDAO.create(resident);
            ctx.status(201);
            ctx.header("Location", "/api/residents/" + created.getId());
            ctx.json(toResponseDTO(created));

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create resident", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Resident resident = residentDAO.read(id);
            if (resident == null) {
                ctx.status(404).result("Resident not found with id: " + id);
                return;
            }

            residentDAO.delete(id);
            ctx.status(204);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid ID format");
        } catch (Exception e) {
            logger.error("Failed to delete resident", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long aLong) {
        return false;
    }

    @Override
    public Resident validateEntity(Context ctx) {
        return null;
    }

    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Resident resident = residentDAO.read(id);

            if (resident == null) {
                ctx.status(404).result("Resident not found with id: " + id);
                return;
            }

            ctx.status(200).json(toResponseDTO(resident));
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid ID format");
        } catch (Exception e) {
            logger.error("Failed to read resident", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            List<Resident> residents = residentDAO.readAll();
            List<ResidentResponseDTO> response = residents.stream()
                    .map(this::toResponseDTO)
                    .toList();

            ctx.json(response);
        } catch (Exception e) {
            logger.error("Failed to get residents", e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void getAllSorted(Context ctx) {
        try {
            User currentUser = getAuthenticatedUser(ctx);
            List<ResidentResponseDTO> residents = residentService.getAllSorted(currentUser);
            ctx.status(200).json(residents);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to fetch sorted residents", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            CreateResidentRequestDTO req = ctx.bodyAsClass(CreateResidentRequestDTO.class);

            Resident existingResident = residentDAO.read(id);
            if (existingResident == null) {
                ctx.status(404).result("Resident not found with id: " + id);
                return;
            }

            if (req.getFirstName() != null) {
                existingResident.setFirstName(req.getFirstName());
            }
            if (req.getLastName() != null) {
                existingResident.setLastName(req.getLastName());
            }
            if (req.getCprNr() != null) {
                existingResident.setCprNr(req.getCprNr());
            }

            Resident updated = residentDAO.update(id, existingResident);
            ctx.status(200).json(toResponseDTO(updated));
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid ID format");
        } catch (Exception e) {
            logger.error("Failed to update resident", e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void deactivate(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Resident resident = residentDAO.read(id);
            if (resident == null) {
                ctx.status(404).result("Resident not found with id: " + id);
                return;
            }

            residentDAO.deactivate(id);
            ctx.status(204);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid ID format");
        } catch (EntityNotFoundException e) {
            ctx.status(404).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to deactivate resident", e);
            ctx.status(500).result("Internal server error");
        }
    }

    private User getAuthenticatedUser(Context ctx) {
        var tokenUser = ctx.attribute("user");
        String email = null;

        if (tokenUser instanceof com.carebridge.dtos.JwtUserDTO ju) {
            email = ju.getUsername();
        } else if (tokenUser instanceof com.carebridge.dtos.UserDTO du) {
            email = du.getEmail();
        } else if (tokenUser != null) {
            email = tokenUser.toString();
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Authenticated user is required");
        }

        User user = userDAO.readByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Authenticated user not found");
        }

        return user;
    }

    private ResidentResponseDTO toResponseDTO(Resident resident) {
        Long journalId = resident.getJournal() != null ? resident.getJournal().getId() : null;
        Long medicationChartId = resident.getMedicationChart() != null ? resident.getMedicationChart().getId() : null;

        return new ResidentResponseDTO(
                resident.getId(),
                resident.getFirstName(),
                resident.getLastName(),
                resident.getCprNr(),
                resident.getAge(),
                resident.getGender(),
                journalId,
                medicationChartId
        );
    }
}
