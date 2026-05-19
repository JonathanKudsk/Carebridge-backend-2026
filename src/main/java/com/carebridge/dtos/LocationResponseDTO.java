package com.carebridge.dtos;

import com.carebridge.entities.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDTO {
    private long id;
    private String locationName;
    private String Address;
    private CityResponseDTO city;

    public LocationResponseDTO(Location location) {
        this.id = location.getId();
        this.locationName = location.getLocationName();
        this.Address = location.getAddress();
        this.city = location.getCity() == null ? null : new CityResponseDTO(location.getCity());
    }
}
