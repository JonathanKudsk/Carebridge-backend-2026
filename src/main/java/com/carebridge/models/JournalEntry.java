package com.carebridge.models;

import com.carebridge.enums.RiskAssessment;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to Resident (many entries per resident)
    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Constructors ---
    public JournalEntry() {}

    public JournalEntry(Resident resident, User author, String title, String content, RiskAssessment riskAssessment) {
        this.resident = resident;
        this.author = author;
        this.title = title;
        this.content = content;
        this.riskAssessment = riskAssessment;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }

    public Resident getResident() { return resident; }
    public void setResident(Resident resident) { this.resident = resident; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
