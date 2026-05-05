package com.carebridge.services.mappers;

import com.carebridge.dtos.ChatRoomDTO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.entities.ChatRoomUser;

import java.util.List;

// Converts chat rooms to API DTOs and back while keeping member lists intact.
public final class ChatRoomMapper {

    private ChatRoomMapper() {
    }

    public static ChatRoomDTO toDTO(ChatRoom entity) {
        if (entity == null) {
            return null;
        }

        List<com.carebridge.dtos.ChatRoomUserDTO> members = entity.getChatRoomUser() == null
                ? List.of()
                : entity.getChatRoomUser().stream().map(ChatRoomUserMapper::toDTO).toList();

        return ChatRoomDTO.builder()
                .id(entity.getId())
                .members(members)
                .build();
    }

    public static ChatRoom toEntity(ChatRoomDTO dto, List<ChatRoomUser> members) {
        if (dto == null) {
            return null;
        }

        ChatRoom entity = new ChatRoom();
        entity.setChatRoomUser(members == null ? List.of() : members);
        return entity;
    }
}

