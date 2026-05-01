package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ChatRoomDAO;
import com.carebridge.dao.impl.MessageDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.dtos.MessageDTO;
import com.carebridge.entities.Message;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.services.mappers.MessageMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MessageController implements IController<Message, Long> {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageDAO messageDAO = MessageDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final ChatRoomDAO chatRoomDAO = ChatRoomDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            var entity = messageDAO.read(id);
            // Allow filtering by room so the frontend can load one chat thread at a time.
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"Message not found\"}");
                return;
            }
            ctx.json(MessageMapper.toDTO(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read message failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            String chatRoomIdRaw = ctx.queryParam("chatRoomId");
            if (chatRoomIdRaw != null) {
                Long chatRoomId = parseId(chatRoomIdRaw);
                var list = messageDAO.readByChatRoom(chatRoomId).stream().map(MessageMapper::toDTO).toList();
                ctx.json(list);
                return;
            }

            var list = messageDAO.readAll().stream().map(MessageMapper::toDTO).toList();
            ctx.json(list);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("readAll messages failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

//    @Override
//    public void create(Context ctx) {
//        try {
//            MessageDTO dto = ctx.bodyAsClass(MessageDTO.class);
//            // Turn the incoming ids into managed entities before persisting the message.
//            var user = resolveUser(dto.getUserId());
//            var chatRoom = resolveChatRoom(dto.getChatRoomId());
//
//            Message created = messageDAO.create(MessageMapper.toEntity(dto, user, chatRoom));
//            ctx.status(201).json(MessageMapper.toDTO(created));
//        } catch (ApiRuntimeException e) {
//            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
//        } catch (Exception e) {
//            logger.error("create message failed", e);
//            ctx.status(500).json("{\"msg\":\"Internal error\"}");
//        }
//    }

    @Override
    public void create(Context ctx) {
        try {
            JwtUserDTO jwtUser = ctx.attribute("user");
            User sender = userDAO.readByEmail(jwtUser.getUsername());

            if (!sender.isEmployed()) {
                ctx.status(403).json(Map.of("msg", "You are not employed and cannot send messages"));
                return;
            }

            MessageDTO dto = ctx.bodyAsClass(MessageDTO.class);
            // Turn the incoming ids into managed entities before persisting the message.
            var user = resolveUser(dto.getUserId());
            var chatRoom = resolveChatRoom(dto.getChatRoomId());
            if (!chatRoom.isActive())  {
                ctx.status(403).json(Map.of("msg", "This chat room is read-only"));
                return;
            }

            Message created = messageDAO.create(MessageMapper.toEntity(dto, user, chatRoom));
            MessageDTO createdDTO = MessageMapper.toDTO(created);
            ChatRoomWebSocketController.broadcast(dto.getChatRoomId(), createdDTO);
            ctx.status(201).json(MessageMapper.toDTO(created));

        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create failed", e);
            ctx.status(500).json(Map.of("msg", "Internal error"));
        }
    }


    @Override
    public void update(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            MessageDTO dto = ctx.bodyAsClass(MessageDTO.class);

            Message patch = new Message();
            // Only patch fields the client actually sends.
            patch.setMessage(dto.getMessage());
            patch.setTimestamp(dto.getTimestamp());
            if (dto.getUserId() != null) {
                patch.setUser(resolveUser(dto.getUserId()));
            }
            if (dto.getChatRoomId() != null) {
                patch.setChatRoom(resolveChatRoom(dto.getChatRoomId()));
            }

            Message updated = messageDAO.update(id, patch);
            ctx.json(MessageMapper.toDTO(updated));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("update message failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            messageDAO.delete(id);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("delete message failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public Message validateEntity(Context ctx) {
        return ctx.bodyAsClass(Message.class);
    }

    private Long parseId(String idRaw) {
        try {
            Long id = Long.parseLong(idRaw);
            if (!validatePrimaryKey(id)) {
                throw new ApiRuntimeException(400, "Invalid id");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ApiRuntimeException(400, "Invalid id");
        }
    }

    private com.carebridge.entities.User resolveUser(Long userId) {
        if (userId == null) {
            throw new ApiRuntimeException(400, "userId is required");
        }
        // Make sure the message always points to a real user.
        var user = userDAO.read(userId);
        if (user == null) {
            throw new ApiRuntimeException(404, "User not found: " + userId);
        }
        return user;
    }

    private com.carebridge.entities.ChatRoom resolveChatRoom(Long chatRoomId) {
        if (chatRoomId == null) {
            throw new ApiRuntimeException(400, "chatRoomId is required");
        }
        // Make sure the message always points to a real room.
        var room = chatRoomDAO.read(chatRoomId);
        if (room == null) {
            throw new ApiRuntimeException(404, "Chat room not found: " + chatRoomId);
        }
        return room;
    }
}

