package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFieldDTO {
    private String title;
    private String fieldType; // TEXTFIELD, CHECKBOX, NUMBERFIELD
}
