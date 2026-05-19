package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLocationRequestDTO {
    private String locationName;
    private String address;
    private CreateCityRequestDTO city;
}
