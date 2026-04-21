package com.carebridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MessageDTO {
    private Long id;
    private Long userId;
    private Long chatRoomId;
    private String message;
    private Timestamp timestamp;

}
