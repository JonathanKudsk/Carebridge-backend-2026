package com.carebridge.crud.data.core;

import com.carebridge.crud.annotations.Children;
import com.carebridge.crud.annotations.Parent;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [DATA LAYER]
 * Base entity providing JPA identity and hierarchical relationships.
 */
@Entity
@Table(name = "base_entities")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BaseEntity parent;

    @Children
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BaseEntity> children = new ArrayList<>();

    public BaseEntity getParent() { return parent; }
    public void setParent(BaseEntity parent) { this.parent = parent; }
    public List<BaseEntity> getChildren() { return children; }
    public void setChildren(List<BaseEntity> children) { this.children = children; }

    /**
     * Helper to get the grandparent of this entity.
     */
    public Optional<BaseEntity> getGrandparent() {
        return Optional.ofNullable(parent).map(BaseEntity::getParent);
    }

    /**
     * Helper to get all grandchildren of this entity.
     */
    public List<BaseEntity> getGrandchildren() {
        return children.stream()
                .flatMap(child -> child.getChildren().stream())
                .collect(Collectors.toList());
    }
}
