package com.carebridge.entities;

import com.carebridge.enums.FieldType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="template_id",nullable = false)
    private Template template;

    @Column(name="title")
    private String title;

    @Column(name="type")
    private FieldType fieldType;
}
