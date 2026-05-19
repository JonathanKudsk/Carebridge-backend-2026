package com.carebridge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import lombok.*;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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
            joinColumns = @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "fk_location_user_location")),
            inverseJoinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_location_user_user"))
    )
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @ManyToOne
    @Setter(AccessLevel.NONE)
    private City city;

    // ========== CONSTRUCTORS ==========

    public Location(String locationName, String address) {
        this.locationName = locationName;
        this.address = address;
    }

    // ========== Methods ==========
    public void setCity (City city){
        if (this.city != city){
            this.city = city;
            city.AddLocation(this);
        }
    }

    public void addUser (User user){
        if(users.add(user)){
            user.addLocation(this);
        }
    }

    public void removeUser (User user){
        if(users.remove(user)){
            user.removeLocation(this);
        }
    }

    @Override
    public String toString() {
        return "Location{" +
                "address='" + address + '\'' +
                ", locationName='" + locationName + '\'' +
                '}';
    }

    // ========== EQUALS & HASHCODE ==========


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(locationName, location.locationName) && Objects.equals(address, location.address) && Objects.equals(city, location.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationName, address, city);
    }
}
