package com.carebridge.services.mappers;

import com.carebridge.dtos.SubstituteLocationDTO;
import com.carebridge.dtos.SubstituteResponseDTO;
import com.carebridge.entities.Location;
import com.carebridge.entities.User;

import java.util.Comparator;
import java.util.List;

public final class SubstituteMapper {
    private SubstituteMapper() {
    }

    public static SubstituteResponseDTO toDTO(User substitute) {
        if (substitute == null) return null;
        return SubstituteResponseDTO.builder()
                .id(substitute.getId())
                .name(substitute.getName())
                .email(substitute.getEmail())
                .displayEmail(substitute.getDisplayEmail())
                .displayPhone(substitute.getDisplayPhone())
                .internalEmail(substitute.getInternalEmail())
                .internalPhone(substitute.getInternalPhone())
                .locations(toLocationDTOs(substitute.getLocations().stream().toList()))
                .build();
    }

    public static SubstituteLocationDTO toLocationDTO(Location location) {
        if (location == null) return null;
        return SubstituteLocationDTO.builder()
                .id(location.getId())
                .locationName(location.getLocationName())
                .address(location.getAddress())
                .build();
    }

    public static List<SubstituteLocationDTO> toLocationDTOs(List<Location> locations) {
        return locations.stream()
                .map(SubstituteMapper::toLocationDTO)
                .sorted(Comparator.comparing(SubstituteLocationDTO::getLocationName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }
}
