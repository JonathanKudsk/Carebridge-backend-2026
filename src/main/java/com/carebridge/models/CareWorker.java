package entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class CareWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    // Mange CareWorkers ↔ mange Residents
    @ManyToMany
    @JoinTable(
            name = "careworker_resident",
            joinColumns = @JoinColumn(name = "careworker_id"),
            inverseJoinColumns = @JoinColumn(name = "resident_id")
    )
    private List<Resident> residents;

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Resident> getResidents() { return residents; }
    public void setResidents(List<Resident> residents) { this.residents = residents; }
}
