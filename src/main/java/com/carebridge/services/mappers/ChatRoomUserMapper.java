package com.carebridge.services.mappers;

import com.carebridge.dtos.ChatRoomUserDTO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.entities.ChatRoomUser;
import com.carebridge.entities.User;

// Flattens chat room membership data so the frontend only needs ids.
public final class ChatRoomUserMapper {

    private ChatRoomUserMapper() {
    }

    public static ChatRoomUserDTO toDTO(ChatRoomUser entity) {
        if (entity == null) {
            return null;
        }

        return ChatRoomUserDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .chatRoomId(entity.getChatRoom() != null ? entity.getChatRoom().getId() : null)
                .build();
    }

    public static ChatRoomUser toEntity(ChatRoomUserDTO dto, User user, ChatRoom chatRoom) {
        if (dto == null) {
            return null;
        }

        ChatRoomUser entity = new ChatRoomUser();
        entity.setUser(user);
        entity.setChatRoom(chatRoom);
        return entity;
    }
}

