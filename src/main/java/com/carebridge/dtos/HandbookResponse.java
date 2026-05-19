package com.carebridge.dtos;

import java.util.List;

public class HandbookResponse {
    private Long id;
    private String title;
    private List<HandbookTabResponse> handbookTabs;

    public HandbookResponse(Long id, String title, List<HandbookTabResponse> handbookTabs) {
        this.id = id;
        this.title = title;
        this.handbookTabs = handbookTabs;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public List<HandbookTabResponse> getHandbookTabs() { return handbookTabs; }
}