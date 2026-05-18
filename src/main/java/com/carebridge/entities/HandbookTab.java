package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "handbook_tab")
@Getter
@Setter
@NoArgsConstructor
public class HandbookTab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handbook_id", nullable = false)
    private Handbook handbook;

    @Column(nullable = false, length = 255)
    private String title;

    // Stores HTML content from the Tiptap editor
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    public HandbookTab(String title, int orderIndex) {
        this.title = title;
        this.content = "";
        this.orderIndex = orderIndex;
    }
}