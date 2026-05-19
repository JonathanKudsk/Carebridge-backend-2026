package com.carebridge.dtos;

public class HandbookTabResponse {
    private Long id;
    private String title;
    private String content;
    private int orderIndex;

    public HandbookTabResponse(Long id, String title, String content, int orderIndex) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.orderIndex = orderIndex;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getOrderIndex() { return orderIndex; }
}