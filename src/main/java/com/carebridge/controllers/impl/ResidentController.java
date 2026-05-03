package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.CreateResidentRequestDTO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.services.mappers.ResidentMapper;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ResidentController implements IController<Resident, Long> {

    private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);
    private final ResidentDAO residentDAO = ResidentDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();

    public ResidentController() {
    }

    // Create resident (POST /api/residents)
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

            // create single linked journal
            Journal journal = new Journal();
            resident.setJournal(journal);
            // Important: set the back-reference on the owning side
            journal.setResident(resident);

            // --- Extract authenticated user and attach to resident/journal if desired ---
            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof com.carebridge.dtos.JwtUserDTO ju) email = ju.getUsername();
            else if (tokenUser instanceof com.carebridge.dtos.UserDTO du) email = du.getEmail();
            else if (tokenUser != null) email = tokenUser.toString();

            if (email != null) {
                User user = userDAO.readByEmail(email);
                if (user != null) {
                    // attach user to resident (many-to-many)
                    resident.addUser(user);
                    // if you also want to mark journal creator, add a setter on Journal and set it
                    // journal.setCreatedBy(user);
                }
            }
            // -----------------------------------------------------------------------

            Resident created = residentDAO.create(resident);

            Long journalId = created.getJournal() != null ? created.getJournal().getId() : null;
            ResidentResponseDTO resp = new ResidentResponseDTO(
                    created.getId(),
                    created.getFirstName(),
                    created.getLastName(),
                    journalId
            );

            ctx.status(201);
            ctx.header("Location", "/api/residents/" + created.getId());
            ctx.json(resp);

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create resident", e);
            ctx.status(500).result("Internal server error");
        }
    }

    // Unused interface methods kept minimal
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
    public boolean validatePrimaryKey(Long aLong) { return false; }

    @Override
    public Resident validateEntity(Context ctx) { return null; }

    @Override
    public void read(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Resident resident = residentDAO.read(id);

            if (resident == null) {
                ctx.status(404).result("Resident not found with id: " + id);
                return;
            }

            Long journalId = resident.getJournal() != null ? resident.getJournal().getId() : null;
            ResidentResponseDTO resp = new ResidentResponseDTO(
                    resident.getId(),
                    resident.getFirstName(),
                    resident.getLastName(),
                    journalId,
                    resident.getCprNr()
            );
            ctx.status(200).json(resp);

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

            ctx.json(ResidentMapper.toDTOList(residents));
        } catch (Exception e) {
            logger.error("Failed to read all residents", e);
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

            if (req.getFirstName() != null) existingResident.setFirstName(req.getFirstName());
            if (req.getLastName() != null) existingResident.setLastName(req.getLastName());
            if (req.getCprNr() != null) existingResident.setCprNr(req.getCprNr());

            Resident updated = residentDAO.update(id, existingResident);

            Long journalId = updated.getJournal() != null ? updated.getJournal().getId() : null;
            ResidentResponseDTO resp = new ResidentResponseDTO(
                    updated.getId(),
                    updated.getFirstName(),
                    updated.getLastName(),
                    journalId
            );

            ctx.status(200).json(resp);

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
}
