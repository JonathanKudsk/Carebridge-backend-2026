package com.carebridge.entities;

import com.carebridge.enums.RiskAssessment;
import com.carebridge.enums.EntryType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to User (the CareWorker or Admin who wrote it)
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "journalEntry", cascade = CascadeType.PERSIST)
    private List<JournalEntryAnswer> journalEntryAnswers;

    @Column(nullable = false, length = 255)
    private String title;

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

    public JournalEntry(Journal journal, User author, String title, RiskAssessment riskAssessment, EntryType entryType, Template template) {
        this.journal = journal;
        this.author = author;
        this.title = title;
        this.riskAssessment = riskAssessment;
        this.entryType = entryType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.editCloseTime = this.createdAt.plusHours(24);
        this.template = template;
    }
}