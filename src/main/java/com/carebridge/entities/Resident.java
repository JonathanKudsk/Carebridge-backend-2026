package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String roomNumber;

    private LocalDate dateOfBirth;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "resident_medical_conditions",
            joinColumns = @JoinColumn(name = "resident_id"))
    @Column(name = "condition")
    @Builder.Default
    private List<String> medicalConditions = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_id")
    private Journal journal;

    // Many-to-Many relationship med Guardians
    @ManyToMany(mappedBy = "residents")
    @Builder.Default
    private List<User> guardians = new ArrayList<>();
}