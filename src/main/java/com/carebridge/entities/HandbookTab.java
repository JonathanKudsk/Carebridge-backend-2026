package com.carebridge.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "handbook_tab")
public class HandbookTab
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handbook_id", nullable = false)
    private Handbook handbook;

    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Stores HTML content from the Tiptap editor.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "required_role", nullable = false, length = 100)
    private String requiredRole;




    public HandbookTab(
            String title,
            String content,
            int orderIndex,
            String requiredRole
    )
    {
        this.title = title;
        this.content = content;
        this.orderIndex = orderIndex;
        this.requiredRole = requiredRole;
    }

    public HandbookTab() {

    }


    public Long getId()
    {
        return id;
    }


    public Handbook getHandbook()
    {
        return handbook;
    }


    public void setHandbook(Handbook handbook)
    {
        this.handbook = handbook;
    }


    public String getTitle()
    {
        return title;
    }


    public void setTitle(String title)
    {
        this.title = title;
    }


    public String getContent()
    {
        return content;
    }


    public void setContent(String content)
    {
        this.content = content;
    }


    public int getOrderIndex()
    {
        return orderIndex;
    }


    public void setOrderIndex(int orderIndex)
    {
        this.orderIndex = orderIndex;
    }


    public void setRequiredRole(String requiredRole)
    {
        this.requiredRole = requiredRole;
    }
}
