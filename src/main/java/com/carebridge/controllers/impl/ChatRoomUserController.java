package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ChatRoomDAO;
import com.carebridge.dao.impl.ChatRoomUserDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.ChatRoomUserDTO;
import com.carebridge.entities.ChatRoomUser;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.services.mappers.ChatRoomUserMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatRoomUserController implements IController<ChatRoomUser, Long> {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomUserController.class);
    private final ChatRoomUserDAO chatRoomUserDAO = ChatRoomUserDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final ChatRoomDAO chatRoomDAO = ChatRoomDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            var entity = chatRoomUserDAO.read(id);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"Membership not found\"}");
                return;
            }
            ctx.json(ChatRoomUserMapper.toDTO(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read chat room membership failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            String roomIdRaw = ctx.queryParam("chatRoomId");
            String userIdRaw = ctx.queryParam("userId");

            // Support simple filtering so the frontend can ask for one room or one user.
            if (roomIdRaw != null) {
                Long roomId = parseId(roomIdRaw);
                var list = chatRoomUserDAO.readByChatRoom(roomId).stream().map(ChatRoomUserMapper::toDTO).toList();
                ctx.json(list);
                return;
            }

            if (userIdRaw != null) {
                Long userId = parseId(userIdRaw);
                var list = chatRoomUserDAO.readByUser(userId).stream().map(ChatRoomUserMapper::toDTO).toList();
                ctx.json(list);
                return;
            }

            var list = chatRoomUserDAO.readAll().stream().map(ChatRoomUserMapper::toDTO).toList();
            ctx.json(list);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("readAll chat room memberships failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            ChatRoomUserDTO dto = ctx.bodyAsClass(ChatRoomUserDTO.class);
            // Resolve ids to managed entities before creating the membership row.
            var user = resolveUser(dto.getUserId());
            var room = resolveChatRoom(dto.getChatRoomId());

            ChatRoomUser created = chatRoomUserDAO.create(ChatRoomUserMapper.toEntity(dto, user, room));
            ctx.status(201).json(ChatRoomUserMapper.toDTO(created));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create chat room membership failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            ChatRoomUserDTO dto = ctx.bodyAsClass(ChatRoomUserDTO.class);

            ChatRoomUser patch = new ChatRoomUser();
            // Update only the parts the client actually changed.
            if (dto.getUserId() != null) {
                patch.setUser(resolveUser(dto.getUserId()));
            }
            if (dto.getChatRoomId() != null) {
                patch.setChatRoom(resolveChatRoom(dto.getChatRoomId()));
            }

            ChatRoomUser updated = chatRoomUserDAO.update(id, patch);
            ctx.json(ChatRoomUserMapper.toDTO(updated));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("update chat room membership failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            chatRoomUserDAO.delete(id);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("delete chat room membership failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public ChatRoomUser validateEntity(Context ctx) {
        return ctx.bodyAsClass(ChatRoomUser.class);
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
        // Keep the controller strict about ids so bad requests fail early.
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
        // Keep the controller strict about ids so bad requests fail early.
        var room = chatRoomDAO.read(chatRoomId);
        if (room == null) {
            throw new ApiRuntimeException(404, "Chat room not found: " + chatRoomId);
        }
        return room;
    }
}

