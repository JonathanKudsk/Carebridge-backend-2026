package com.carebridge.dtos;

import com.carebridge.entities.Template;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor

public class TemplateDetailedResponseDTO {
    @EqualsAndHashCode.Exclude
    private Long id;
    private String title;
    private FieldResponseDTO[] fields;

    public TemplateDetailedResponseDTO(Template template) {
        this.id = template.getId();
        this.title = template.getTitle();
        this.fields = template.getFields().stream().map(FieldResponseDTO::new).toArray(FieldResponseDTO[]::new);
    }
}

