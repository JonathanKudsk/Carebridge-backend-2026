package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffJournalResponseDTO {
    private Long id;
    private Long userId;
    private List<StaffJournalContractDTO> contracts;
    private List<StaffJournalEntryResponseDTO> entries;
}
