package com.carebridge.services.mappers;

import com.carebridge.dtos.CreateResidentRequestDTO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Resident;

import java.util.List;
import java.util.stream.Collectors;

public class ResidentMapper {

    public static Resident toEntity(CreateResidentRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Resident resident = new Resident();
        resident.setFirstName(dto.getFirstName());
        resident.setLastName(dto.getLastName());
        resident.setCprNr(dto.getCprNr());

        return resident;
    }

    public static ResidentResponseDTO toDTO(Resident resident) {
        if (resident == null) {
            return null;
        }

        Long journalId = resident.getJournal() != null ? resident.getJournal().getId() : null;
        Long medicationChartId = resident.getMedicationChart() != null ? resident.getMedicationChart().getId() : null;

        return new ResidentResponseDTO(
                resident.getId(),
                resident.getFirstName(),
                resident.getLastName(),
                resident.getCprNr(),
                resident.getAge(),
                resident.getGender(),
                journalId,
                medicationChartId,
                resident.isActive()
        );
    }

    public static List<ResidentResponseDTO> toDTOList(List<Resident> residents) {
        if (residents == null) {
            return null;
        }

        return residents.stream()
                .map(ResidentMapper::toDTO)
                .collect(Collectors.toList());
    }
}
