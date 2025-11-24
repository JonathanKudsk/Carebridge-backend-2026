package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @ManyToMany
    @JoinTable(
            name = "event_seen_by_users",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> seenByUsers = new HashSet<>();

    public Event() {
    }

    public Event(String title, String description, Instant startAt,
                 boolean showOnBoard, User createdBy, EventType eventType) {
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.showOnBoard = showOnBoard;
        this.createdBy = createdBy;
        this.eventType = eventType;
        syncDateTimeFromStartAt();
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (this.created_at == null) {
            this.created_at = now;
        }
        this.updated_at = now;
        syncDateTimeFromStartAt();
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = Instant.now();
        syncDateTimeFromStartAt();
    }

    private void syncDateTimeFromStartAt() {
        if (this.startAt == null) {
            this.eventDate = null;
            this.eventTime = null;
            return;
        }
        ZoneId zone = ZoneId.of("Europe/Copenhagen");
        ZonedDateTime zdt = this.startAt.atZone(zone);
        this.eventDate = zdt.toLocalDate();
        this.eventTime = zdt.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
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
        syncDateTimeFromStartAt();
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

    public LocalDate getEventDate() {
        return eventDate;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public Set<User> getSeenByUsers() {
        return seenByUsers;
    }

    public void setSeenByUsers(Set<User> seenByUsers) {
        this.seenByUsers = seenByUsers;
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
}
