package com.carebridge.models;

import jakarta.persistence.*;

@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    // Mange Tags → 1 JournalEntry
    @ManyToOne
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;

    // Getters + Setters
    public Long getId() { return id; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public JournalEntry getJournalEntry() { return journalEntry; }
    public void setJournalEntry(JournalEntry journalEntry) { this.journalEntry = journalEntry; }
}
