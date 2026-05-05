package com.carebridge.services.mappers;

import com.carebridge.dtos.MessageDTO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.entities.Message;
import com.carebridge.entities.User;

// Converts message entities to a compact DTO with only ids and message text.
public final class MessageMapper {

    private MessageMapper() {
    }

    public static MessageDTO toDTO(Message entity) {
        if (entity == null) {
            return null;
        }

        return MessageDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .chatRoomId(entity.getChatRoom() != null ? entity.getChatRoom().getId() : null)
                .message(entity.getMessage())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public static Message toEntity(MessageDTO dto, User user, ChatRoom chatRoom) {
        if (dto == null) {
            return null;
        }

        Message entity = new Message();
        entity.setUser(user);
        entity.setChatRoom(chatRoom);
        entity.setMessage(dto.getMessage());
        entity.setTimestamp(dto.getTimestamp());
        return entity;
    }
}

