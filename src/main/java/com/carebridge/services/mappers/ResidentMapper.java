package com.carebridge.services.mappers;

import com.carebridge.dtos.CreateResidentRequestDTO;
import com.carebridge.dtos.GuardianDTO;
import com.carebridge.dtos.ResidentDetailsResponseDTO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;

import java.time.Instant;
import java.util.ArrayList;
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

    public static ResidentDetailsResponseDTO toDetailsDTO(Resident resident) {
        if (resident == null) {
            return null;
        }

        Long journalId = (resident.getJournal() != null) ? resident.getJournal().getId() : null;
        Long medicationChartId = (resident.getMedicationChart() != null) ? resident.getMedicationChart().getId() : null;

        Long userId = null;
        String userName = null;
        String displayPhone = null;
        String displayEmail = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        if (resident.getUser() != null) {
            User user = resident.getUser();
            userId = user.getId();
            userName = user.getDisplayName();
            displayPhone = user.getDisplayPhone();
            displayEmail = user.getDisplayEmail();
            createdAt = user.getCreated_at();
            updatedAt = user.getUpdated_at();
        }

        List<GuardianDTO> guardianDTOs = new ArrayList<>();
        if (resident.getUsers() != null) {
            for (User guardianUser : resident.getUsers()) {
                guardianDTOs.add(new GuardianDTO(
                        guardianUser.getId(),
                        guardianUser.getName(),
                        guardianUser.getDisplayPhone(),
                        guardianUser.getDisplayEmail(),
                        guardianUser.getDisplayName()
                ));
            }
        }


        return new ResidentDetailsResponseDTO(
                resident.getId(),
                resident.getFirstName(),
                resident.getLastName(),
                resident.getCprNr(),
                resident.getAge(),
                resident.getGender(),
                resident.isActive(),
                journalId,
                medicationChartId,
                userId,
                userName,
                displayPhone,
                displayEmail,
                createdAt,
                updatedAt,
                guardianDTOs
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
