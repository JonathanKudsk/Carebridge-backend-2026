package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateJournalEntryAnswerRequestDTO {
    private String answer;
    private Long fieldId;
}
