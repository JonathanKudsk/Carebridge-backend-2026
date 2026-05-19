package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagedChatRoomsDTO {
    private List<ChatRoomDTO> chatRooms;
    private long totalCount;
}
