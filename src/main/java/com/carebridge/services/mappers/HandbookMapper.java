package com.carebridge.services.mappers;

import com.carebridge.dtos.HandbookResponse;
import com.carebridge.dtos.HandbookTabResponse;
import com.carebridge.entities.Handbook;
import com.carebridge.entities.HandbookTab;

public class HandbookMapper {

    public static HandbookResponse toResponse(Handbook handbook) {
        return new HandbookResponse(
            handbook.getId(),
            handbook.getTitle(),
            handbook.getHandbookTabs().stream()
                .map(HandbookMapper::toTabResponse)
                .toList()
        );
    }

    public static HandbookTabResponse toTabResponse(HandbookTab tab) {
        return new HandbookTabResponse(
            tab.getId(),
            tab.getTitle(),
            tab.getContent(),
            tab.getOrderIndex()
        );
    }
}