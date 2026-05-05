package com.carebridge.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "template", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Field> fields = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "template")
    private List<JournalEntry> journalEntries;

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
