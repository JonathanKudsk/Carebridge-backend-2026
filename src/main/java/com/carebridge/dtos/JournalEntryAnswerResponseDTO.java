package com.carebridge.dtos;

import com.carebridge.entities.JournalEntryAnswer;
import lombok.Getter;

@Getter
public class JournalEntryAnswerResponseDTO {
    private Long id;
    private String answer;
    private FieldResponseDTO field;

    public JournalEntryAnswerResponseDTO(JournalEntryAnswer journalEntryAnswer) {
        this.id = journalEntryAnswer.getId();
        this.answer = journalEntryAnswer.getAnswer();
        this.field = new FieldResponseDTO(journalEntryAnswer.getField());
    }
}
