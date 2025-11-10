package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
    private LocalDate eventDate;
    private LocalTime eventTime;
    private boolean seenByCurrentUser;
    private List<Long> seenByUserIds;
}
