package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "cityname", nullable = false)
    @Getter
    @Setter
    private String cityname;

    @Column(name = "postalcode", nullable = false)
    @Getter
    @Setter
    private int postalcode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "location_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_city_location")
    )
    @Getter
    @Setter
    private Location location;

    // ========== CONSTRUCTORS ==========

    public City() {}

    public City(String cityname, int postalcode, Location location) {
        this.cityname = cityname;
        this.postalcode = postalcode;
        this.location = location;
    }

    // ========== EQUALS & HASHCODE ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}