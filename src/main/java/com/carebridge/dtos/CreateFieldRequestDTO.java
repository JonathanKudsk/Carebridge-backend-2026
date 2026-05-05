package com.carebridge.dtos;

import com.carebridge.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFieldRequestDTO {
    String title;
    FieldType fieldType;
}
