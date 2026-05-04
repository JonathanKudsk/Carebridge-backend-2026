package com.carebridge.services.mappers;

import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Resident;

import java.util.List;
import java.util.stream.Collectors;

public class ResidentMapper {

    public static ResidentResponseDTO toDTO(Resident r) {
        if (r == null) return null;

        Long journalId = (r.getJournal() != null) ? r.getJournal().getId() : null;

        return new ResidentResponseDTO(
                r.getId(),
                r.getFirstName(),
                r.getLastName(),
                journalId,
                r.getCprNr(),
                r.isActive()
        );
    }

    public static List<ResidentResponseDTO> toDTOList(List<Resident> residents) {
        if (residents == null) return null;
        return residents.stream()
                .map(ResidentMapper::toDTO)
                .collect(Collectors.toList());
    }
}
