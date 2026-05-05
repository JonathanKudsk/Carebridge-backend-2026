package com.carebridge.entities;

import com.carebridge.enums.FieldType;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder

@Table(name = "field")
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
