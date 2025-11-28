package com.carebridge.controllers.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.LinkResidentsRequest;
import com.carebridge.dtos.UserDTO;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.services.mappers.UserMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserController implements IController<User, Long> {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserDAO userDAO = UserDAO.getInstance();
    private final ResidentDAO residentDAO = ResidentDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try {
            Long id = validatePrimaryKey(ctx.pathParam("id")) ? Long.parseLong(ctx.pathParam("id")) : null;
            User entity = userDAO.read(id);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"User not found\"}");
                return;
            }
            ctx.json(UserMapper.toDTO(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read user failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            var list = userDAO.readAll().stream().map(UserMapper::toDTO).collect(Collectors.toList());
            ctx.json(list);
        } catch (Exception e) {
            logger.error("readAll users failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            var dto = ctx.bodyAsClass(UserDTO.class);
            var entity = UserMapper.toEntity(dto);
            var created = userDAO.create(entity);
            ctx.status(201).json(UserMapper.toDTO(created));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create user failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = validatePrimaryKey(ctx.pathParam("id")) ? Long.parseLong(ctx.pathParam("id")) : null;
            var dto = ctx.bodyAsClass(UserDTO.class);
            var patch = UserMapper.toEntity(dto);
            var updated = userDAO.update(id, patch);
            if (updated == null) {
                ctx.status(404).json("{\"msg\":\"User not found\"}");
                return;
            }
            ctx.json(UserMapper.toDTO(updated));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("update user failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = validatePrimaryKey(ctx.pathParam("id")) ? Long.parseLong(ctx.pathParam("id")) : null;
            userDAO.delete(id);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("delete user failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }




    public void me(Context ctx) {
        try {
            var jwtUser = ctx.attribute("user");
            if (jwtUser == null) {
                ctx.status(401).json("{\"msg\":\"Unauthorized\"}");
                return;
            }
            String email = null;
            if (jwtUser instanceof com.carebridge.dtos.JwtUserDTO ju) email = ju.getUsername();
            else if (jwtUser instanceof com.carebridge.dtos.UserDTO ud) email = ud.getEmail();

            if (email == null) {
                ctx.status(401).json("{\"msg\":\"Unauthorized\"}");
                return;
            }

            var entity = userDAO.readByEmail(email);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"User not found\"}");
                return;
            }
            ctx.json(UserMapper.toDTO(entity));
        } catch (Exception e) {
            logger.error("me failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void populate(Context ctx) {
        try {
            Populator.populate(HibernateConfig.getEntityManagerFactory());
            ctx.status(200).json("{\"msg\":\"Database populated\"}");
        } catch (Exception e) {
            logger.error("Populate failed", e);
            ctx.status(500).json("{\"msg\":\"Populate failed\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    public boolean validatePrimaryKey(String idStr) {
        try {
            return idStr != null && Long.parseLong(idStr) > 0;
        } catch (Exception e) {
            return false;
        }
    }


    public void linkResidents(Context ctx) {
        try {
            Long guardianId = Long.parseLong(ctx.pathParam("id"));
            LinkResidentsRequest request = ctx.bodyAsClass(LinkResidentsRequest.class);

            User guardian = userDAO.read(guardianId);
            if (guardian == null) {
                ctx.status(404).json(Map.of("msg", "Guardian ikke fundet"));
                return;
            }

            if (guardian.getRole() != Role.GUARDIAN) {
                ctx.status(400).json(Map.of("msg", "Brugeren er ikke en pårørende"));
                return;
            }

            // Hent residents
            List<Resident> residentsToLink = new ArrayList<>();
            for (Long residentId : request.getResidentIds()) {
                Resident resident = residentDAO.read(residentId);
                if (resident != null) {
                    residentsToLink.add(resident);
                }
            }

            // Tilknyt residents til guardian
            guardian.getResidents().addAll(residentsToLink);
            userDAO.update(guardianId, guardian);

            ctx.status(200).json(Map.of("msg", "Beboere tilknyttet", "count", residentsToLink.size()));

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("msg", "Ugyldigt ID"));
        } catch (Exception e) {
            logger.error("Link residents failed", e);
            ctx.status(500).json(Map.of("msg", "Kunne ikke tilknytte beboere"));
        }
    }

    @Override
    public User validateEntity(Context ctx) {
        return ctx.bodyAsClass(User.class);
    }
}
