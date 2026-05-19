package com.carebridge.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "handbook")
public class Handbook
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "handbook",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("orderIndex ASC") //ordering by orderIndex from handbookTabs
    private List<HandbookTab> handbookTabs = new ArrayList<>();


    public Handbook()
    {
        this.createdAt = LocalDateTime.now();
    }


    public Handbook(String title)
    {
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }

    //Bidirectional relation
    public void addHandbookTab(HandbookTab handbookTab)
    {
        handbookTabs.add(handbookTab);
        handbookTab.setHandbook(this);
    }


    public void removeHandbookTab(HandbookTab handbookTab)
    {
        handbookTabs.remove(handbookTab);
        handbookTab.setHandbook(null);
    }


    public Long getId()
    {
        return id;
    }


    public String getTitle()
    {
        return title;
    }


    public void setTitle(String title)
    {
        this.title = title;
    }


    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }


    public List<HandbookTab> getHandbookTabs()
    {
        return handbookTabs;
    }


    public void setHandbookTabs(List<HandbookTab> handbookTabs)
    {
        this.handbookTabs = handbookTabs;
    }
}
