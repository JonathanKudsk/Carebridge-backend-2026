package com.carebridge.dtos;

import java.util.List;

public class MedicationChartResponseDTO {

    private Long id;
    private Long residentId;
    private List<MedicationResponseDTO> medications;

    public MedicationChartResponseDTO() {}

    public MedicationChartResponseDTO(Long id, Long residentId, List<MedicationResponseDTO> medications) {
        this.id = id;
        this.residentId = residentId;
        this.medications = medications;
    }

    public Long getId() { return id; }
    public Long getResidentId() { return residentId; }
    public List<MedicationResponseDTO> getMedications() { return medications; }
}
