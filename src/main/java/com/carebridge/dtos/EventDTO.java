package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private Instant startAt;
    private boolean showOnBoard;
    private Long createdById;
    private Long eventTypeId;
}
