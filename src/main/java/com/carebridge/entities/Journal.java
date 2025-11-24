package com.carebridge.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1 Journal → mange entries
    @OneToMany(mappedBy = "journal",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<JournalEntry> entries;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", referencedColumnName = "id")
    private Resident resident;

    // Bi-directional relationship - adding an entry to the journal
    public void addEntry(JournalEntry entry) {
        if(entry != null) {
            entries.add(entry);
            entry.setJournal(this);
        }
    }

    // Getters + Setters
    public Long getId() { return id; }
    public List<JournalEntry> getEntries() { return entries; }
    public void setEntries(List<JournalEntry> entries) { this.entries = entries; }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Resident getResident()
    {
        return resident;
    }

    public void setResident(Resident resident)
    {
        this.resident = resident;
    }
}