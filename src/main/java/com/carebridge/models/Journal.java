package entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 1 Journal ↔ 1 Resident
    @OneToOne
    @JoinColumn(name = "resident_id")
    private Resident resident;

    // 1 Journal → mange entries
    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL)
    private List<JournalEntry> entries;

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Resident getResident() { return resident; }
    public void setResident(Resident resident) { this.resident = resident; }
    public List<JournalEntry> getEntries() { return entries; }
    public void setEntries(List<JournalEntry> entries) { this.entries = entries; }
}
