package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "cities")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "cityname", nullable = false)
    private String cityname;

    @Column(name = "postalcode", nullable = false)
    private int postalcode;

    @Builder.Default
    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Location> locations = new HashSet<>();

    // ========== CONSTRUCTORS ==========

    public City(String cityname, int postalcode) {
        this.cityname = cityname;
        this.postalcode = postalcode;
    }

    // ========== Methods ==========
    public void AddLocation (Location location){
        if(locations.add(location)){
            location.setCity(this);
        }
    }

    // ========== EQUALS & HASHCODE ==========


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return postalcode == city.postalcode && Objects.equals(cityname, city.cityname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityname, postalcode);
    }
}
