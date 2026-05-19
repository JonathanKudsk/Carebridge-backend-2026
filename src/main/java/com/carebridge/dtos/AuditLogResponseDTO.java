package com.carebridge.dtos;

import java.time.Instant;

public class AuditLogResponseDTO {

    private Long id;
    private Instant timestamp;
    private Long doctorId;
    private Long medicationId;
    private Long chartId;
    private String action;
    private String description;

    public AuditLogResponseDTO() {}

    public AuditLogResponseDTO(Long id, Instant timestamp, Long doctorId, Long medicationId,
                                Long chartId, String action, String description) {
        this.id = id;
        this.timestamp = timestamp;
        this.doctorId = doctorId;
        this.medicationId = medicationId;
        this.chartId = chartId;
        this.action = action;
        this.description = description;
    }

    public Long getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public Long getDoctorId() { return doctorId; }
    public Long getMedicationId() { return medicationId; }
    public Long getChartId() { return chartId; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
}
