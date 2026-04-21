package com.carebridge.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
    private List<ChatRoomUser> chatRoomUser = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public List<ChatRoomUser> getChatRoomUser() {
        return chatRoomUser;
    }

    public void setChatRoomUser(List<ChatRoomUser> chatRoomUsers) {
        this.chatRoomUser = chatRoomUsers;
    }
}

