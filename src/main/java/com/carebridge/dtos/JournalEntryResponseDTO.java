package com.carebridge.dtos;

import com.carebridge.enums.RiskAssessment;

import java.time.LocalDateTime;

public class JournalEntryResponseDTO {

    private Long id;
    private Long residentId;
    private Long authorUserId;
    private String title;
    private String content;
    private RiskAssessment riskAssessment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JournalEntryResponseDTO() {}

    public JournalEntryResponseDTO(Long id, Long residentId, Long authorUserId,
                                   String title, String content,
                                   RiskAssessment riskAssessment,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.residentId = residentId;
        this.authorUserId = authorUserId;
        this.title = title;
        this.content = content;
        this.riskAssessment = riskAssessment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters only (response objects are usually read-only)
    public Long getId() { return id; }
    public Long getResidentId() { return residentId; }
    public Long getAuthorUserId() { return authorUserId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
