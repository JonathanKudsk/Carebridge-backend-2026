package com.carebridge.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter

@Entity
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String cprNr;
    private Integer age;
    private String gender;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToOne(mappedBy = "resident", cascade = CascadeType.ALL)
    private Journal journal;

    @OneToOne(mappedBy = "resident", cascade = CascadeType.ALL)
    private MedicationChart medicationChart;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "resident_user",
            joinColumns = @JoinColumn(name = "resident_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dosage> dosages = new ArrayList<>();

    public Resident() {}

    public Resident(String firstName, String lastName, String cprNr, Journal journal, User guardian) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cprNr = cprNr;
        this.journal = journal;
        this.users = users != null ? users : new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCprNr() {
        return cprNr;
    }

    public void setCprNr(String cprNr) {
        this.cprNr = cprNr;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Dosage> getDosages() {
        return dosages;
    }

    public void setDosages(List<Dosage> dosages) {
        this.dosages = dosages;
    }

    public MedicationChart getMedicationChart() {
        return medicationChart;
    }

    public void setMedicationChart(MedicationChart medicationChart) {
        this.medicationChart = medicationChart;
    }
}
