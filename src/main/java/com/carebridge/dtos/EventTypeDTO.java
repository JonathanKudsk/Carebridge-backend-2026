package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventTypeDTO {
    private Long id;
    private String name;
    private String colorHex;
}
