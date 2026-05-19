package com.carebridge.services;

import com.carebridge.dao.impl.HandbookDAO;
import com.carebridge.dao.impl.HandbookTabDAO;
import com.carebridge.dtos.HandbookResponse;
import com.carebridge.dtos.HandbookTabResponse;
import com.carebridge.entities.Handbook;
import com.carebridge.entities.HandbookTab;
import com.carebridge.services.mappers.HandbookMapper;

public class HandbookService {

    private final HandbookDAO handbookDAO = HandbookDAO.getInstance();
    private final HandbookTabDAO handbookTabDAO = HandbookTabDAO.getInstance();

    private static HandbookService instance;

    public static synchronized HandbookService getInstance() {
        if (instance == null) {
            instance = new HandbookService();
        }
        return instance;
    }

    public HandbookResponse getHandbook(Long id) {
        return HandbookMapper.toResponse(handbookDAO.read(id));
    }

    public HandbookTabResponse updateTabContent(Long tabId, String content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        HandbookTab tab = handbookTabDAO.findById(tabId);
        tab.setContent(content);

        return HandbookMapper.toTabResponse(handbookTabDAO.update(tab));
    }

    public HandbookTabResponse updateTabTitle(Long tabId, String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        HandbookTab tab = handbookTabDAO.findById(tabId);
        tab.setTitle(title);

        return HandbookMapper.toTabResponse(handbookTabDAO.update(tab));
    }

    public HandbookTabResponse createTab(Long handbookId, String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        Handbook handbook = handbookDAO.read(handbookId);
        int orderIndex = handbookTabDAO.countByHandbookId(handbookId);
        HandbookTab tab = new HandbookTab(title, orderIndex);
        handbook.addHandbookTab(tab);
        return HandbookMapper.toTabResponse(handbookTabDAO.create(tab));
    }

    public void deleteTab(Long tabId) {
        handbookTabDAO.delete(tabId);
    }
}