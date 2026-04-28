package com.carebridge.dtos;

import com.carebridge.entities.Field;
import com.carebridge.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FieldResponseDTO {
    private Long id;
    private String title;
    private FieldType fieldType;

    public FieldResponseDTO(Field field) {
        this.id = field.getId();
        this.title = field.getTitle();
        this.fieldType = field.getFieldType();
    }
}
