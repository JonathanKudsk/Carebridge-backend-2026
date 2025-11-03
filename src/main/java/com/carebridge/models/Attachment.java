package com.carebridge.models;

import jakarta.persistence.*;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storagePath;
    private String mimeType;
    private String originalName;
    private Long sizeBytes;

    // Mange Attachments → 1 JournalEntry
    @ManyToOne
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;

    // Getters + Setters
    public Long getId() { return id; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public JournalEntry getJournalEntry() { return journalEntry; }
    public void setJournalEntry(JournalEntry journalEntry) { this.journalEntry = journalEntry; }
}
