package com.carebridge.dtos;

import com.carebridge.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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

    private Long residentId;
    private Boolean isPrivate;
    private String accessLevel;
    private List<Long> usersWithAccessIds;
    private Integer riskLevel;
    private String riskColor;
    private String riskDescription;

}
