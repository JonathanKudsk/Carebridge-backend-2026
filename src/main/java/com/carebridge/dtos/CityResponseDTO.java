package com.carebridge.dtos;

import com.carebridge.entities.City;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityResponseDTO {
    private long id;
    private String cityName;
    private int postalCode;

    public CityResponseDTO(City city) {
        this.id = city.getId();
        this.cityName = city.getCityname();
        this.postalCode = city.getPostalcode();
    }
}
