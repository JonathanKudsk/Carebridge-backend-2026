package com.carebridge.entities;

import com.carebridge.enums.FieldType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity

@Table(name = "fields")
public class Field {

    @Id
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name="template_id",nullable = false)
    @JsonBackReference
    @EqualsAndHashCode.Exclude
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return Objects.equals(template, field.template) && Objects.equals(title, field.title) && fieldType == field.fieldType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(template, title, fieldType);
    }
}
