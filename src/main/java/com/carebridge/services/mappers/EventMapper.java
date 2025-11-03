package com.carebridge.services.mappers;

import com.carebridge.dtos.EventDTO;
import com.carebridge.entities.Event;

public class EventMapper {
    public static EventDTO toDTO(Event event) {
        if (event == null) return null;
        return new EventDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartAt(),
                event.isShowOnBoard(),
                event.getCreatedBy() != null ? event.getCreatedBy().getId() : null,
                event.getEventType() != null ? event.getEventType().getId() : null
        );
    }
}
