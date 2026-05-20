package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffJournalEntryRequestDTO {
    private String title;
    private String content;
}
