package com.carebridge.controllers.impl;

import com.carebridge.services.HandbookService;
import io.javalin.http.Context;

import java.util.Map;

public class HandbookController {

    private final HandbookService service = HandbookService.getInstance();

    // Currently we only work with 1 handbook according to the US.
    public void getHandbook(Context ctx) {
        ctx.json(service.getHandbook(1L));
    }

    public void updateTabContent(Context ctx) {
        Long tabId = Long.parseLong(ctx.pathParam("tabId"));
        Map<String, String> body = ctx.bodyAsClass(Map.class);
        String content = body.get("content");
        ctx.json(service.updateTabContent(tabId, content));
    }

    public void updateTabTitle(Context ctx) {
        Long tabId = Long.parseLong(ctx.pathParam("tabId"));
        Map<String, String> body = ctx.bodyAsClass(Map.class);
        String title = body.get("title");
        ctx.json(service.updateTabTitle(tabId, title));
    }

    public void createTab(Context ctx) {
        Map<String, Object> body = ctx.bodyAsClass(Map.class);
        Long handbookId = Long.parseLong(body.get("handbookId").toString());
        String title = body.get("title").toString();
        ctx.status(201);
        ctx.json(service.createTab(handbookId, title));
    }

    public void deleteTab(Context ctx) {
        Long tabId = Long.parseLong(ctx.pathParam("tabId"));
        service.deleteTab(tabId);
        ctx.status(204);
    }
}