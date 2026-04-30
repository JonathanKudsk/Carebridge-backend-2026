package com.carebridge.dtos;

import com.carebridge.entities.Template;
import lombok.Getter;

@Getter
public class TemplateResponseDTO {
    private Long id;
    private String title;

    public TemplateResponseDTO(Template template) {
        this.id = template.getId();
        this.title = template.getTitle();
    }
}


