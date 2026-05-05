package com.carebridge.controllers.impl;

import com.carebridge.utils.Utils;
import io.javalin.websocket.WsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomWebSocketController.class);
    private static final Map<Long, Set<WsContext>> rooms = new ConcurrentHashMap<>();

    public static void onConnect(WsContext ctx) {
        Long roomId = roomId(ctx);
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(ctx);
        logger.info("WS client connected to room {}", roomId);
    }

    public static void onClose(WsContext ctx) {
        Long roomId = roomId(ctx);
        Set<WsContext> clients = rooms.get(roomId);
        if (clients != null) {
            clients.remove(ctx);
            if (clients.isEmpty()) rooms.remove(roomId);
        }
        logger.info("WS client disconnected from room {}", roomId);
    }

    public static void broadcast(Long chatRoomId, Object payload) {
        Set<WsContext> clients = rooms.get(chatRoomId);
        if (clients == null || clients.isEmpty()) return;
        try {
            String json = new Utils().getObjectMapper().writeValueAsString(payload);
            clients.removeIf(ctx -> !ctx.session.isOpen());
            clients.forEach(ctx -> ctx.send(json));
            logger.info("Broadcast message to {} client(s) in room {}", clients.size(), chatRoomId);
        } catch (Exception e) {
            logger.error("WS broadcast failed for room {}", chatRoomId, e);
        }
    }

    private static Long roomId(WsContext ctx) {
        return Long.parseLong(ctx.pathParam("id"));
    }
}
