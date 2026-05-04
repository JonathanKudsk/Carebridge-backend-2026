package com.carebridge.entities;

import com.carebridge.enums.FieldType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity

@Table(name = "field")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name="template_id",nullable = false)
    @JsonBackReference
    private Template template;

    @Column(name="title")
    private String title;

    @Column(name="type")
    private FieldType fieldType;

    public void setTemplate(Template template) {
        this.template = template;
        if(!template.getFields().contains(this)){
            template.addField(this);
        }
    }
}
