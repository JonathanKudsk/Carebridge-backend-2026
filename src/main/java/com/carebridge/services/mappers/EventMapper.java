package com.carebridge.services.mappers;

import com.carebridge.dtos.EventDTO;
import com.carebridge.entities.Event;
import com.carebridge.entities.User;

import java.util.List;
import java.util.Objects;

public class EventMapper {

    public static EventDTO toDTO(Event event) {
        return toDTO(event, null);
    }

    public static EventDTO toDTO(Event event, Long currentUserId) {
        if (event == null) return null;

        boolean seenByCurrentUser = false;
        if (currentUserId != null && event.getSeenByUsers() != null) {
            seenByCurrentUser = event.getSeenByUsers().stream()
                    .map(User::getId)
                    .anyMatch(id -> Objects.equals(id, currentUserId));
        }

        List<Long> seenIds = event.getSeenByUsers() == null
                ? List.of()
                : event.getSeenByUsers().stream().map(User::getId).toList();

        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartAt(event.getStartAt());
        dto.setShowOnBoard(event.isShowOnBoard());
        dto.setCreatedById(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null);
        dto.setEventTypeId(event.getEventType() != null ? event.getEventType().getId() : null);

        dto.setEventDate(event.getEventDate());
        dto.setEventTime(event.getEventTime());
        dto.setSeenByCurrentUser(seenByCurrentUser);
        dto.setSeenByUserIds(seenIds);

        return dto;
    }
}
