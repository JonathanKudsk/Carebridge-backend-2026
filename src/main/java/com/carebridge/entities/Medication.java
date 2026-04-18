package com.carebridge.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Medication entity representing a prescribed medication.
 */
@Entity
@Table(name = "medications")
public class Medication implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String dosage; // e.g. "500 mg"
    private String frequency; // e.g. "Twice a day"
    private LocalDate startDate;
    private LocalDate endDate;
    private String prescribingDoctor;
    @Column(length = 2000)
    private String notes;
    private boolean active = true;

    // No-arg constructor required by JPA
    public Medication() {
    }

    // All-args constructor (id can be null for new entities)
    public Medication(Long id, String name, String dosage, String frequency,
                      LocalDate startDate, LocalDate endDate,
                      String prescribingDoctor, String notes, boolean active) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.prescribingDoctor = prescribingDoctor;
        this.notes = notes;
        this.active = active;
    }

    // Convenience constructor without id
    public Medication(String name, String dosage, String frequency,
                      LocalDate startDate, LocalDate endDate,
                      String prescribingDoctor, String notes, boolean active) {
        this(null, name, dosage, frequency, startDate, endDate, prescribingDoctor, notes, active);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPrescribingDoctor() {
        return prescribingDoctor;
    }

    public void setPrescribingDoctor(String prescribingDoctor) {
        this.prescribingDoctor = prescribingDoctor;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Equals/hashCode based on id (suitable for JPA entities)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medication)) return false;
        Medication that = (Medication) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31 + (id != null ? id.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Medication{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", prescribingDoctor='" + prescribingDoctor + '\'' +
                ", active=" + active +
                '}';
    }
}
