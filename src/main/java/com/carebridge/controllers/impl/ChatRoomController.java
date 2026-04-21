package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ChatRoomDAO;
import com.carebridge.dtos.ChatRoomDTO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.exceptions.ApiRuntimeException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class ChatRoomController implements IController<ChatRoom, Long>{
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);
    private final ChatRoomDAO chatRoomDAO = ChatRoomDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try{
            Long id = parseId(ctx);
            var entity = chatRoomDAO.read(id);

            if (entity == null){
               ctx.status(404).json("{\"msg\":\"ChatRoom not found\"}");
               return;
            }
            ctx.json(toDTO(entity));


        } catch (ApiRuntimeException e){
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        }catch (Exception e){
            logger.error("Read chatroom failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }

    }

    @Override
    public void readAll(Context ctx) {
        try{
            var ChatRooms = chatRoomDAO.readAll()
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            ctx.json(ChatRooms);

        }catch (Exception e){
            logger.error("Read all ChatRooms failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            ChatRoom created = chatRoomDAO.create(new ChatRoom());
            ctx.status(201).json(toDTO(created));

        } catch (Exception e){
            logger.error("Create ChatRoom failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = parseId(ctx);
            ChatRoom existing = chatRoomDAO.read(id);

            if(existing == null){
                ctx.status(404).json("{\"msg\":\"ChatRoom not found\"}");
                return;
            }

            ChatRoom updated = chatRoomDAO.update(id, existing);

            ctx.json(toDTO(updated));
        }catch (ApiRuntimeException e){
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage());

        }catch (Exception e){
            logger.error("Update ChatRoom failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try{
            Long id = parseId(ctx);
            chatRoomDAO.delete(id);
            ctx.status(204);

        } catch (ApiRuntimeException e){
           ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        }catch (Exception e){
            logger.error("Delete ChatRoom failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    private ChatRoomDTO toDTO(ChatRoom entity){
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setId(entity.getId());
        return dto;
    }

    private Long parseId(Context ctx){
        try{
            return Long.parseLong(ctx.pathParam("id"));
        }catch(Exception e){
            throw new ApiRuntimeException(400, "Invalid id");
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
}
