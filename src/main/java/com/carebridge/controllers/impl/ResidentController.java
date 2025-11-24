package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.CreateResidentRequestDTO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            // if Journal had a back-reference: journal.setResident(resident);

            // --- Extract authenticated user and attach to resident/journal if desired ---
            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof com.carebridge.dtos.JwtUserDTO ju) email = ju.getUsername();
            else if (tokenUser instanceof com.carebridge.dtos.UserDTO du) email = du.getEmail();
            else if (tokenUser != null) email = tokenUser.toString();

            if (email != null) {
                User user = userDAO.readByEmail(email);
                if (user != null) {
                    // TODO: attach the user depending on your data model
                    // Examples (uncomment and adapt if such setters exist):
                    // resident.setAssignedUser(user);
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
    public void delete(Context ctx) { throw new UnsupportedOperationException(); }

    @Override
    public boolean validatePrimaryKey(Long aLong) { return false; }

    @Override
    public Resident validateEntity(Context ctx) { return null; }

    @Override
    public void read(Context ctx) { throw new UnsupportedOperationException(); }

    @Override
    public void readAll(Context ctx) { throw new UnsupportedOperationException(); }

    @Override
    public void update(Context ctx) { throw new UnsupportedOperationException(); }
}
