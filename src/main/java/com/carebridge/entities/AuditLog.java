package com.carebridge.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private Long doctorId;

    private Long medicationId;
    private Long chartId;

    @Column(nullable = false)
    private String action;

    @Column(length = 2000)
    private String description;

    public AuditLog() {}

    public AuditLog(Long doctorId, Long medicationId, Long chartId, String action, String description) {
        this.doctorId = doctorId;
        this.medicationId = medicationId;
        this.chartId = chartId;
        this.action = action;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }

    public Long getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public Long getDoctorId() { return doctorId; }
    public Long getMedicationId() { return medicationId; }
    public Long getChartId() { return chartId; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
}
