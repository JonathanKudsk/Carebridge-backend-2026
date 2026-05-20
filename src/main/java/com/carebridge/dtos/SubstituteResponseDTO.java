package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubstituteResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String displayEmail;
    private String displayPhone;
    private String internalEmail;
    private String internalPhone;
    private List<SubstituteLocationDTO> locations;
}
