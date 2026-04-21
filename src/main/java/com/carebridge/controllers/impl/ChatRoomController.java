package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ChatRoomDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.ChatRoomDTO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.entities.ChatRoomUser;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.services.mappers.ChatRoomMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ChatRoomController implements IController<ChatRoom, Long> {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);
    private final ChatRoomDAO chatRoomDAO = ChatRoomDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            ChatRoom entity = chatRoomDAO.read(id);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"Chat room not found\"}");
                return;
            }
            ctx.json(ChatRoomMapper.toDTO(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read chat room failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            // Return rooms as DTOs so the frontend gets member ids instead of entities.
            var list = chatRoomDAO.readAll().stream().map(ChatRoomMapper::toDTO).toList();
            ctx.json(list);
        } catch (Exception e) {
            logger.error("readAll chat rooms failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            ChatRoomDTO dto = ctx.bodyAsClass(ChatRoomDTO.class);
            var members = new ArrayList<ChatRoomUser>();

            // Convert incoming user ids into managed users before creating the room.
            if (dto.getMembers() != null) {
                for (var memberDTO : dto.getMembers()) {
                    if (memberDTO.getUserId() == null) {
                        throw new ApiRuntimeException(400, "member userId is required");
                    }

                    var user = userDAO.read(memberDTO.getUserId());
                    if (user == null) {
                        throw new ApiRuntimeException(404, "User not found: " + memberDTO.getUserId());
                    }

                    ChatRoomUser member = new ChatRoomUser();
                    member.setUser(user);
                    members.add(member);
                }
            }

            ChatRoom created = chatRoomDAO.create(ChatRoomMapper.toEntity(dto, members));
            ctx.status(201).json(ChatRoomMapper.toDTO(created));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create chat room failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            ChatRoomDTO dto = ctx.bodyAsClass(ChatRoomDTO.class);
            var members = new ArrayList<ChatRoomUser>();

            // Updating a room means replacing the membership list with the new one.
            if (dto.getMembers() != null) {
                for (var memberDTO : dto.getMembers()) {
                    if (memberDTO.getUserId() == null) {
                        throw new ApiRuntimeException(400, "member userId is required");
                    }

                    var user = userDAO.read(memberDTO.getUserId());
                    if (user == null) {
                        throw new ApiRuntimeException(404, "User not found: " + memberDTO.getUserId());
                    }

                    ChatRoomUser member = new ChatRoomUser();
                    member.setUser(user);
                    members.add(member);
                }
            }

            ChatRoom patch = ChatRoomMapper.toEntity(dto, members);
            ChatRoom updated = chatRoomDAO.update(id, patch);
            ctx.json(ChatRoomMapper.toDTO(updated));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("update chat room failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx.pathParam("id"));
            chatRoomDAO.delete(id);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("delete chat room failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public ChatRoom validateEntity(Context ctx) {
        return ctx.bodyAsClass(ChatRoom.class);
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
}

