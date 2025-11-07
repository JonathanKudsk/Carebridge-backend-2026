package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @Size(max = 4000)
    @Column(columnDefinition = "text")
    private String description;

    @NotNull
    @FutureOrPresent(message = "start_at cannot be in the past")
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "show_on_board", nullable = false)
    private boolean showOnBoard = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_events_users_created_by")
    )
    private User createdBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "event_type_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_events_event_types")
    )
    private EventType eventType;

    @Column(nullable = false, updatable = false)
    private Instant created_at;

    @Column(nullable = false)
    private Instant updated_at;

    public Event() {
    }

    public Event(String title, String description, Instant startAt, boolean showOnBoard, User createdBy, EventType eventType) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.showOnBoard = showOnBoard;
        this.createdBy = createdBy;
        this.eventType = eventType;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.created_at = now;
        this.updated_at = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public boolean isShowOnBoard() {
        return showOnBoard;
    }

    public void setShowOnBoard(boolean showOnBoard) {
        this.showOnBoard = showOnBoard;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }
}
