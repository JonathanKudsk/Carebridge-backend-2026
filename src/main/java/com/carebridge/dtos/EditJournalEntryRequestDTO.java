package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditJournalEntryRequestDTO
{
    private String title;
    private String riskAssessment;
    private String entryType;
    private CreateJournalEntryAnswerRequestDTO[] answers;

}
