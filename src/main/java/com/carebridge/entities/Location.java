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
    @Column(name = "afdelingsnavn", nullable = false)
    private String afdelingsnavn;

    @NotBlank
    @Size(max = 255)
    @Column(name = "addresse", nullable = false)
    private String addresse;

    @ManyToMany
    @JoinTable(
            name = "location_vikar",
            joinColumns = @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "fk_location_vikar_location")),
            inverseJoinColumns = @JoinColumn(name = "vikar_id", foreignKey = @ForeignKey(name = "fk_location_vikar_vikar"))
    )
    private Set<Vikar> vikarer = new HashSet<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<By> byer = new HashSet<>();

    // ========== CONSTRUCTORS ==========

    public Location() {}

    public Location(String afdelingsnavn, String addresse) {
        this.afdelingsnavn = afdelingsnavn;
        this.addresse = addresse;
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

    public String getAfdelingsnavn() {
        return afdelingsnavn;
    }

    public void setAfdelingsnavn(String afdelingsnavn) {
        this.afdelingsnavn = afdelingsnavn;
    }

    public String getAddresse() {
        return addresse;
    }

    public void setAddresse(String addresse) {
        this.addresse = addresse;
    }

    public Set<Vikar> getVikarer() {
        return vikarer;
    }

    public void setVikarer(Set<Vikar> vikarer) {
        this.vikarer = vikarer;
    }

    public Set<By> getByer() {
        return byer;
    }

    public void setByer(Set<By> byer) {
        this.byer = byer;
    }
}
