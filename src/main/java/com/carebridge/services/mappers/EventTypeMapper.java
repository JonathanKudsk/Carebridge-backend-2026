package com.carebridge.services.mappers;

import com.carebridge.dtos.EventTypeDTO;
import com.carebridge.entities.EventType;

public class EventTypeMapper {
    public static EventTypeDTO toDTO(EventType type) {
        if (type == null) return null;
        return new EventTypeDTO(
                type.getId(),
                type.getName(),
                type.getColorHex()
        );
    }
}
