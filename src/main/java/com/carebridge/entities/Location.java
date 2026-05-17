package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "location_name", nullable = false)
    private String locationName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "address", nullable = false)
    private String address;

    @ManyToMany
    @JoinTable(
            name = "location_substitute",
            joinColumns = @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "fk_location_substitute_location")),
            inverseJoinColumns = @JoinColumn(name = "substitute_id", foreignKey = @ForeignKey(name = "fk_location_substitute_substitute"))
    )
    private Set<Substitute> substitutes = new HashSet<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<By> byer = new HashSet<>();

    // ========== CONSTRUCTORS ==========

    public Location() {}

    public Location(String locationName, String address) {
        this.locationName = locationName;
        this.address = address;
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location other)) return false;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Set<Substitute> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(Set<Substitute> substitutes) {
        this.substitutes = substitutes;
    }

    public Set<By> getByer() {
        return byer;
    }

    public void setByer(Set<By> byer) {
        this.byer = byer;
    }
}
