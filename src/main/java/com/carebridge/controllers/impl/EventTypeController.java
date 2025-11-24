package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.dtos.EventTypeDTO;
import com.carebridge.entities.EventType;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.services.mappers.EventTypeMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class EventTypeController implements IController<EventType, Long> {

    private static final Logger logger = LoggerFactory.getLogger(EventTypeController.class);
    private final EventTypeDAO eventTypeDAO = EventTypeDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx);
            var entity = eventTypeDAO.read(id);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"EventType not found\"}");
                return;
            }
            ctx.json(EventTypeMapper.toDTO(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read eventType failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            var list = eventTypeDAO.readAll().stream().map(EventTypeMapper::toDTO).collect(Collectors.toList());
            ctx.json(list);
        } catch (Exception e) {
            logger.error("readAll eventTypes failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            EventTypeDTO dto = ctx.bodyAsClass(EventTypeDTO.class);
            var entity = new EventType(dto.getName(), dto.getColorHex());
            var created = eventTypeDAO.create(entity);
            ctx.status(201).json(EventTypeMapper.toDTO(created));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create eventType failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = parseId(ctx);
            EventTypeDTO dto = ctx.bodyAsClass(EventTypeDTO.class);
            var patch = new EventType();
            patch.setName(dto.getName());
            patch.setColorHex(dto.getColorHex());
            var updated = eventTypeDAO.update(id, patch);
            if (updated == null) {
                ctx.status(404).json("{\"msg\":\"EventType not found\"}");
                return;
            }
            ctx.json(EventTypeMapper.toDTO(updated));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("update eventType failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx);
            eventTypeDAO.delete(id);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("delete eventType failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public EventType validateEntity(Context ctx) {
        return ctx.bodyAsClass(EventType.class);
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (Exception e) {
            throw new ApiRuntimeException(400, "Invalid id");
        }
    }
}
