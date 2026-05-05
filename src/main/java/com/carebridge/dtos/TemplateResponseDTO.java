package com.carebridge.dtos;

import com.carebridge.entities.Template;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TemplateResponseDTO {
    @EqualsAndHashCode.Exclude
    private Long id;
    private String title;

    public TemplateResponseDTO(Template template) {
        this.id = template.getId();
        this.title = template.getTitle();
    }
}


