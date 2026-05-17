package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(name = "byer")
public class By {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "bynavn", nullable = false)
    private String bynavn;

    @Column(name = "postnummer", nullable = false)
    private int postnummer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "location_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_by_location")
    )
    private Location location;

    // ========== CONSTRUCTORS ==========

    public By() {}

    public By(String bynavn, int postnummer, Location location) {
        this.bynavn = bynavn;
        this.postnummer = postnummer;
        this.location = location;
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof By other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ========== GETTERS & SETTERS ==========

    public Long getId() {
        return id;
    }

    public String getBynavn() {
        return bynavn;
    }

    public void setBynavn(String bynavn) {
        this.bynavn = bynavn;
    }

    public int getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(int postnummer) {
        this.postnummer = postnummer;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}