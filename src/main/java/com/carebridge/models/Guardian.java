package com.carebridge.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Guardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 1 Guardian → mange Residents
    @OneToMany(mappedBy = "guardian")
    private List<Resident> residents;

    // Getters + Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Resident> getResidents() { return residents; }
    public void setResidents(List<Resident> residents) { this.residents = residents; }
}
