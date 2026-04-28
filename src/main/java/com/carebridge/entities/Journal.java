package com.carebridge.entities;

import com.carebridge.crud.annotations.CrudResource;
import com.carebridge.crud.data.core.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@CrudResource(path = "journals")
public class Journal extends BaseEntity {

    // 1 Journal → mange entries
    @OneToMany(mappedBy = "journal",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<JournalEntry> entries = new java.util.ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", referencedColumnName = "id")
    private Resident resident;

    // Bi-directional relationship - adding an entry to the journal
    public void addEntry(@NotNull JournalEntry entry) {
            entries.add(entry);
            entry.setJournal(this);
        
    }

    // Getters + Setters
    public List<JournalEntry> getEntries() { return entries; }
    public void setEntries(List<JournalEntry> entries) { this.entries = entries; }

    public Resident getResident()
    {
        return resident;
    }

    public void setResident(Resident resident)
    {
        this.resident = resident;
    }
}