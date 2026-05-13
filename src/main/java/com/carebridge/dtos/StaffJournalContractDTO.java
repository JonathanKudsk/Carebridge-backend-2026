package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffJournalContractDTO {
    private Long id;
    private String filename;
    private LocalDateTime uploadedAt;
}
