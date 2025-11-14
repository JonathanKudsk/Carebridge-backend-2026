package com.carebridge.models;

import com.carebridge.enums.RiskAssessment;
import com.carebridge.enums.EntryType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to User (the CareWorker or Admin who wrote it)
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_assessment", nullable = false, length = 10)
    private RiskAssessment riskAssessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "edit_close_time", nullable = false)
    private LocalDateTime editCloseTime;

    // Mange JournalEntries → 1 Journal
    @ManyToOne
    @JoinColumn(name = "journal_id")
    private Journal journal;
    // --- Constructors ---
    public JournalEntry() {}

    public JournalEntry(User author, String title, String content, RiskAssessment riskAssessment, EntryType entryType) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.riskAssessment = riskAssessment;
        this.entryType = entryType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.editCloseTime = this.createdAt.plusHours(24);
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getEditCloseTime() { return editCloseTime; }
    public void setEditCloseTime(LocalDateTime editCloseTime) { this.editCloseTime = editCloseTime; }

    public Journal getJournal() { return journal; }
    public void setJournal(Journal journal) { this.journal = journal; }

}
