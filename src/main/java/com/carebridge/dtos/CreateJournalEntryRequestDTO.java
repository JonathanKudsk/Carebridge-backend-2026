package com.carebridge.dtos;


import com.carebridge.enums.RiskAssessment;

public class CreateJournalEntryRequestDTO {

    private Long journalId;
    private Long authorUserId;
    private String title;
    private String content;
    private RiskAssessment riskAssessment;

    public CreateJournalEntryRequestDTO() {}

    public CreateJournalEntryRequestDTO(Long journalId, Long authorUserId,
                                        String title, String content,
                                        RiskAssessment riskAssessment) {
        this.journalId = journalId;
        this.authorUserId = authorUserId;
        this.title = title;
        this.content = content;
        this.riskAssessment = riskAssessment;
    }

    // Getters & setters
    //Setters are only for setting objects when parsing through the http to dto

    public Long getJournalId() { return journalId; }
    public Long setJournalId(Long journalId) { return journalId; }

    public Long getAuthorUserId() { return authorUserId; }
    public Long setAuthorUserId(Long authorUserId) { return authorUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }
}
