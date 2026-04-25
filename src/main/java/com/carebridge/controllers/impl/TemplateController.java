package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dtos.TemplateDetailedResponseDTO;
import com.carebridge.dtos.TemplateResponseDTO;
import com.carebridge.entities.Field;
import com.carebridge.entities.JournalEntry;
import com.carebridge.entities.Template;
import com.carebridge.enums.FieldType;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class TemplateController implements IController<Template, Long> {
    @Override
    public void read(Context ctx) {
        //todo: implement beyond hardcoded example
        ArrayList<Field> fields = new ArrayList<>();
        ArrayList<JournalEntry> journalEntries = new ArrayList<>();
        Template template = new Template(1L,fields, journalEntries,"example");
        fields.add(new Field(1L,template,"ExampleTEXT", FieldType.TEXTFIELD));
        fields.add(new Field(2L,template,"ExampleBOX", FieldType.CHECKBOX));
        fields.add(new Field(3L,template,"ExampleNUMBER", FieldType.NUMBERFIELD));

        TemplateDetailedResponseDTO responseDTO = new TemplateDetailedResponseDTO(template);

        ctx.status(200);
        ctx.json(responseDTO);
    }

    @Override
    public void readAll(Context ctx) {
        //todo: implement beyond hardcoded example
        ArrayList<Field> fields = new ArrayList<>();
        ArrayList<JournalEntry> journalEntries = new ArrayList<>();
        List<Template> templates = new ArrayList<>();
        templates.add(new Template(1L,fields, journalEntries,"example"));
        templates.add(new Template(2L,fields, journalEntries,"example2"));
        templates.add(new Template(3L,fields, journalEntries,"example3"));

        TemplateResponseDTO[] responseDTO = templates.stream().map(TemplateResponseDTO::new).toArray(TemplateResponseDTO[]::new);
        ctx.status(200);
        ctx.json(responseDTO);
    }

    @Override
    public void create(Context ctx) {
        //todo: implement
        ctx.status(501);
        ctx.json("not yet implemented feature");
    }

    @Override
    public void update(Context ctx) {
        //todo: implement
        ctx.status(501);
        ctx.json("not yet implemented feature");
    }

    @Override
    public void delete(Context ctx) {
        //todo: implement
        ctx.status(501);
        ctx.json("not yet implemented feature");
    }

    @Override
    public boolean validatePrimaryKey(Long aLong) {
        return false;
    }

    @Override
    public Template validateEntity(Context ctx) {
        return null;
    }
}
