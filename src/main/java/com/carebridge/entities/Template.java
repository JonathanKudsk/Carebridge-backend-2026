package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@EqualsAndHashCode

@Table(name = "template")
public class Template {

    @Id
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "template")
    private List<Field> fields;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "template")
    private List<JournalEntry> journalEntries;

    @Column(name="title")
    private String title;

    public void addField(Field f) {
        fields.add(f);
        if(f.getTemplate() != this){
            f.setTemplate(this);
        }
    }
}
