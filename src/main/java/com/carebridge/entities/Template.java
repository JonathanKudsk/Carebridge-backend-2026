package com.carebridge.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@EqualsAndHashCode

@Table(name = "templates")
public class Template {

    @Id
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "template", cascade = CascadeType.ALL)
    @JsonManagedReference
    @Builder.Default
    private List<Field> fields = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "template")
    @Builder.Default
    private List<JournalEntry> journalEntries = new ArrayList<>();

    @Column(name="title")
    private String title;

    @Column(name="is_usable", nullable = false)
    private boolean isUsable = true;

    public void addField(Field f) {
        fields.add(f);
        if(f.getTemplate() != this){
            f.setTemplate(this);
        }
    }
}
