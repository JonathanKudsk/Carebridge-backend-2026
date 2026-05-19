package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "staff_journals")
public class StaffJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "staffJournal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffJournalEntry> entries = new ArrayList<>();

    @OneToMany(mappedBy = "staffJournal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffJournalContract> contracts = new ArrayList<>();
}
