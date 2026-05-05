package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.TemplateDAO;
import com.carebridge.dtos.*;
import com.carebridge.entities.Field;
import com.carebridge.entities.Template;
import com.carebridge.enums.FieldType;
import com.carebridge.exceptions.ApiRuntimeException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class TemplateController implements IController<Template, Long> {

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);
    private final TemplateDAO templateDAO = TemplateDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx);
            Template entity = templateDAO.read(id);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"Template not found\"}");
                return;
            }
            ctx.status(200);
            ctx.json(new TemplateDetailedResponseDTO(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read Template failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            List<Template> entities = templateDAO.readAll();
            if (entities == null || entities.isEmpty()) {
                ctx.status(404).json("{\"msg\":\"Templates not found\"}");
                return;
            }
            TemplateResponseDTO[] responseDTO = entities.stream().map(TemplateResponseDTO::new).toArray(TemplateResponseDTO[]::new);
            ctx.status(200);
            ctx.json(responseDTO);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read Template failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            CreateTemplateRequestDTO dto = ctx.bodyAsClass(CreateTemplateRequestDTO.class);
            if (dto.getTitle() == null || dto.getTitle().isBlank())
                throw new ApiRuntimeException(400, "title required");
            if (dto.getFields() == null || dto.getFields().length == 0)
                throw new ApiRuntimeException(400, "fields required");
            for (CreateFieldRequestDTO fieldDTO : dto.getFields()){
                if (fieldDTO.getTitle() == null || fieldDTO.getTitle().isBlank())
                    throw new ApiRuntimeException(400, "field title required");
                if (fieldDTO.getFieldType() == null)
                    throw new ApiRuntimeException(400, "field title required");
            }

            Template t = Template.builder()
                    .title(dto.getTitle())
                    .build();
            t.setFields(
                    (Arrays.stream(dto.getFields())
                    .map(f -> Field.builder()
                            .fieldType(f.getFieldType())
                            .title(f.getTitle())
                            .template(t)
                            .build())
                    .toList())
            );
            Template created = templateDAO.create(t);
            ctx.status(201).json(new TemplateDetailedResponseDTO(created));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create template failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        //as templates can't get changed without it effecting journal entries it might be best to leave empty to avoid issues
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx);
            templateDAO.delete(id);
            ctx.status(200);
            ctx.json("Template successfully deleted");
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Delete Template failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public Template validateEntity(Context ctx) {
        return null;
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (Exception ex) {
            throw new ApiRuntimeException(400, "Invalid id");
        }
    }
}
