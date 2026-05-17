package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@NoArgsConstructor
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "location_name", nullable = false)
    @Getter
    @Setter
    private String locationName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "address", nullable = false)
    @Getter
    @Setter
    private String address;

    @ManyToMany
    @JoinTable(
            name = "location_substitute",
            joinColumns = @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "fk_location_substitute_location")),
            inverseJoinColumns = @JoinColumn(name = "substitute_id", foreignKey = @ForeignKey(name = "fk_location_substitute_substitute"))
    )
    @Getter
    @Setter
    private Set<Substitute> substitutes = new HashSet<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private Set<City> cities = new HashSet<>();

    // ========== CONSTRUCTORS ==========

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
}
