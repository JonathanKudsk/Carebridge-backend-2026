package com.carebridge.dtos;


import com.carebridge.enums.RiskAssessment;

public class CreateJournalEntryRequestDTO {

    private Long residentId;
    private Long authorUserId;
    private String title;
    private String content;
    private RiskAssessment riskAssessment;

    public CreateJournalEntryRequestDTO() {}

    public CreateJournalEntryRequestDTO(Long residentId, Long authorUserId,
                                        String title, String content,
                                        RiskAssessment riskAssessment) {
        this.residentId = residentId;
        this.authorUserId = authorUserId;
        this.title = title;
        this.content = content;
        this.riskAssessment = riskAssessment;
    }

    // Getters & setters
    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }

    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }
}
