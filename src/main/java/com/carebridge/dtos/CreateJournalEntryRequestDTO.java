package com.carebridge.dtos;


import com.carebridge.entities.JournalEntryAnswer;
import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateJournalEntryRequestDTO {

    private Long journalId;
    private Long authorUserId;
    private String title;
    private EntryType entryType;
    private RiskAssessment riskAssessment;
    private Long TemplateId;
    private CreateJournalEntryAnswerRequestDTO[] answers;
}
