package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity

@Table(name = "template")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "template")
    private List<Field> fields;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "template")
    private List<JournalEntry> journalEntries;

    @Column(name="title")
    private String title;

    @Column(name="is_usable", nullable = false)
    private boolean isUsable = true;
}
