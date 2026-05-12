package com.carebridge.dtos;

import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter // Getters only (response objects are usually read-only)
public class JournalEntryDetailedResponseDTO {

    private Long id;
    private Long journalId;
    private Long authorUserId;
    private String title;
    private EntryType entryType;
    private RiskAssessment riskAssessment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime editCloseTime;
    private JournalEntryAnswerResponseDTO[] journalEntryAnswerResponseDTO;


    public JournalEntryDetailedResponseDTO(Long id, Long journalId, Long authorUserId,
                                           String title, EntryType entryType,
                                           RiskAssessment riskAssessment,
                                           LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime editCloseTime,
                                           JournalEntryAnswerResponseDTO[] journalEntryAnswerResponseDTO) {
        this.id = id;
        this.journalId = journalId;
        this.authorUserId = authorUserId;
        this.title = title;
        this.entryType = entryType;
        this.riskAssessment = riskAssessment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.editCloseTime = editCloseTime;
        this.journalEntryAnswerResponseDTO = journalEntryAnswerResponseDTO;
    }
}