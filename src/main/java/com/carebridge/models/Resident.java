package entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    // Mange Residents → 1 Guardian
    @ManyToOne
    @JoinColumn(name = "guardian_id")
    private Guardian guardian;

    // Mange-til-mange med CareWorkers
    @ManyToMany(mappedBy = "residents")
    private List<CareWorker> careWorkers;

    // 1 Resident ↔ 1 Journal
    @OneToOne(mappedBy = "resident", cascade = CascadeType.ALL)
    private Journal journal;

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Guardian getGuardian() { return guardian; }
    public void setGuardian(Guardian guardian) { this.guardian = guardian; }
    public List<CareWorker> getCareWorkers() { return careWorkers; }
    public void setCareWorkers(List<CareWorker> careWorkers) { this.careWorkers = careWorkers; }
    public Journal getJournal() { return journal; }
    public void setJournal(Journal journal) { this.journal = journal; }
}
