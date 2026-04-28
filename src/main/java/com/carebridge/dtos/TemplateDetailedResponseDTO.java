package com.carebridge.dtos;

import com.carebridge.entities.Template;
import lombok.Getter;

@Getter
public class TemplateDetailedResponseDTO {
    private Long id;
    private String title;
    private FieldResponseDTO[] fields;

    public TemplateDetailedResponseDTO(Template template) {
        this.id = template.getId();
        this.title = template.getTitle();
        this.fields = template.getFields().stream().map(FieldResponseDTO::new).toArray(FieldResponseDTO[]::new);
    }
}

