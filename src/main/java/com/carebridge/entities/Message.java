package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @NotNull
    @Column(nullable = false, updatable = false)
    private Timestamp timestamp;


    @NotNull
    @Size(min = 1, max = 2000) // helps Hibernate validator with string when persist
    @Column(nullable = false)
    private String message;
    // Manuel setter method for boundaries in msg string
    public void setMessage(String message) {
        if(message == null || message.trim().isEmpty() || message.length() > 2000) {
            throw new IllegalArgumentException("Message must be between 1 and 2000 characters");
        }
        this.message = message;
    }

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = new Timestamp(System.currentTimeMillis());
        }
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

}
