package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCityRequestDTO {
    private String cityName;
    private int postalCode;
}
