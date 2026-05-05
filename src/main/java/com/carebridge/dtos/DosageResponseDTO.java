package com.carebridge.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DosageResponseDTO {
    private Long id;
    private String medicineName;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
    private Long residentId;
    private LocalDateTime updatedAt;
    private Long updatedByUserId;

    public DosageResponseDTO() {}

    public DosageResponseDTO(Long id, String medicineName, String dosage, String frequency,
                             LocalDate startDate, LocalDate endDate, String note,
                             Long residentId, LocalDateTime updatedAt, Long updatedByUserId) {
        this.id = id;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.note = note;
        this.residentId = residentId;
        this.updatedAt = updatedAt;
        this.updatedByUserId = updatedByUserId;
    }

    public Long getId() { return id; }
    public String getMedicineName() { return medicineName; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getNote() { return note; }
    public Long getResidentId() { return residentId; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getUpdatedByUserId() { return updatedByUserId; }
}