package com.carebridge.dtos;

import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;

import java.time.LocalDateTime;

public class JournalEntryResponseDTO {

    private Long id;
    private Long journalId;
    private Long authorUserId;
    private String title;
    private String content;
    private EntryType entryType;
    private RiskAssessment riskAssessment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime editCloseTime;

    public JournalEntryResponseDTO() {}

    public JournalEntryResponseDTO(Long id, Long journalId, Long authorUserId,
                                   String title, String content, EntryType entryType,
                                   RiskAssessment riskAssessment,
                                   LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime editCloseTime) {
        this.id = id;
        this.journalId = journalId;
        this.authorUserId = authorUserId;
        this.title = title;
        this.content = content;
        this.entryType = entryType;
        this.riskAssessment = riskAssessment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.editCloseTime = editCloseTime;

    }

    // Getters only (response objects are usually read-only)
    public Long getId() { return id; }
    public Long getJournalId() { return journalId; }
    public Long getAuthorUserId() { return authorUserId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public EntryType getEntryType() { return entryType; }
    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getEditCloseTime() { return editCloseTime; }
}
