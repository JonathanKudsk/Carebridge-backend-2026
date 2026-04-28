package com.carebridge.dtos;

import java.time.LocalDate;

public class MedicationResponseDTO {

    private Long id;
    private Long chartId;
    private String name;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String prescribingDoctor;
    private String notes;
    private boolean active;

    public MedicationResponseDTO() {}

    public MedicationResponseDTO(Long id, Long chartId, String name, String dosage, String frequency,
                                  LocalDate startDate, LocalDate endDate, String prescribingDoctor,
                                  String notes, boolean active) {
        this.id = id;
        this.chartId = chartId;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.prescribingDoctor = prescribingDoctor;
        this.notes = notes;
        this.active = active;
    }

    public Long getId() { return id; }
    public Long getChartId() { return chartId; }
    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getPrescribingDoctor() { return prescribingDoctor; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
}
