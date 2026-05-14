package com.carebridge.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateRequestDTO {
    private String title;
    private CreateFieldRequestDTO[] Fields;
}
