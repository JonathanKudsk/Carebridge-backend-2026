package com.carebridge.services.mappers;

import com.carebridge.dtos.DosageResponseDTO;
import com.carebridge.entities.Dosage;

public class DosageMapper {

    public static DosageResponseDTO toDTO(Dosage dosage) {
        if (dosage == null) return null;

        return new DosageResponseDTO(
                dosage.getId(),
                dosage.getMedicineName(),
                dosage.getDosage(),
                dosage.getFrequency(),
                dosage.getStartDate(),
                dosage.getEndDate(),
                dosage.getNote(),
                dosage.getResident() != null ? dosage.getResident().getId() : null,
                dosage.getUpdatedAt(),
                dosage.getUpdatedByUserId()
        );
    }
}