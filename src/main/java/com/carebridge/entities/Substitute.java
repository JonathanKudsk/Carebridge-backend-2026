package com.carebridge.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "substitutes")
@DiscriminatorValue("SUBSTITUTE")
public class Substitute extends User {

    @ManyToMany(mappedBy = "substitutes")
    private Set<Location> locations = new HashSet<>();

    // ========== CONSTRUCTORS ==========

    public Substitute() {}

    // ========== GETTERS & SETTERS ==========

    public Set<Location> getLocations() {
        return locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }
}
