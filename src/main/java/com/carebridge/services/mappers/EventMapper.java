package com.carebridge.services.mappers;

import com.carebridge.dtos.EventDTO;
import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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


        List<Long> usersWithAccessIds = event.getUsersWithAccess() == null
                ? List.of()
                : event.getUsersWithAccess().stream()
                .map(User::getId)
                .toList();


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

        dto.setResidentId(event.getResidentId());
        dto.setUsersWithAccessIds(usersWithAccessIds);
        dto.setRiskLevel(event.getRiskLevel());
        dto.setRiskColor(event.getRiskColor());
        dto.setRiskDescription(event.getRiskDescription());
        dto.setAccessLevel(event.getAccessLevel());

        dto.setIsPrivate(null);


        return dto;
    }

    public static Event toEntity(EventDTO dto, User creator, EventType eventType, Set<User> usersWithAccess){
        if (dto == null) return null;

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartAt(dto.getStartAt());
        event.setShowOnBoard(dto.isShowOnBoard());
        event.setEventDate(dto.getEventDate());
        event.setEventTime(dto.getEventTime());

        event.setCreatedBy(creator);
        event.setEventType(eventType);

        // Access-felter
        event.setResidentId(dto.getResidentId());
        event.setAccessLevel(resolveAccessLevel(dto.getAccessLevel()));
        event.setRiskLevel(dto.getRiskLevel());
        event.setRiskColor(dto.getRiskColor());
        event.setRiskDescription(dto.getRiskDescription());
        event.setUsersWithAccess(usersWithAccess != null ? usersWithAccess : Set.of());

        return event;
    }

    public static Event updateEntityFromDTO(Event event, EventDTO dto, EventType eventType, Set<User> usersWithAccess) {
        if (event == null || dto == null) return event;

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartAt(dto.getStartAt());
        event.setShowOnBoard(dto.isShowOnBoard());
        event.setEventDate(dto.getEventDate());
        event.setEventTime(dto.getEventTime());
        event.setEventType(eventType);


        event.setResidentId(dto.getResidentId());
        event.setAccessLevel(resolveAccessLevel(dto.getAccessLevel()));
        event.setRiskLevel(dto.getRiskLevel());
        event.setRiskColor(dto.getRiskColor());
        event.setRiskDescription(dto.getRiskDescription());
        event.setUsersWithAccess(usersWithAccess != null ? usersWithAccess : Set.of());

        return event;
    }


    private static String resolveAccessLevel(String accessLevel) {
        return (accessLevel != null && !accessLevel.isBlank()) ? accessLevel : "1";
    }
}
