package com.carebridge.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medication_charts")
public class MedicationChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "medicationChart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Medication> medications = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", referencedColumnName = "id")
    private Resident resident;

    public MedicationChart() {
    }

    public void addMedication(Medication medication) {
        if (medication != null) {
            medications.add(medication);
            medication.setMedicationChart(this);
        }
    }

    public void removeMedication(Medication medication) {
        if (medication != null) {
            medications.remove(medication);
            medication.setMedicationChart(null);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }
}
