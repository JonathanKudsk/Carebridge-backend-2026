package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.EventDAO;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.EventDTO;
import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.services.mappers.EventMapper;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Collectors;

public class EventController implements IController<Event, Long> {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventDAO eventDAO = EventDAO.getInstance();
    private final EventTypeDAO eventTypeDAO = EventTypeDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx);
            var entity = eventDAO.read(id);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"Event not found\"}");
                return;
            }
            ctx.json(EventMapper.toDTO(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read event failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            String fromParam = ctx.queryParam("from");
            String toParam = ctx.queryParam("to");
            String tzParam = ctx.queryParam("tz");

            var tokenUser = ctx.attribute("user");
            Long currentUserId = extractUserIdFromToken(tokenUser);

            if (fromParam != null || toParam != null) {
                ZoneId zone = resolveZone(tzParam);

                var today = LocalDate.now(zone);
                LocalDate fromDate = parseDateKeywordOrIso(fromParam, today, 0);
                LocalDate toDate = parseDateKeywordOrIso(toParam, today, 1);

                Instant fromInstant = fromDate.atStartOfDay(zone).toInstant();
                Instant toInstant = toDate.plusDays(1).atStartOfDay(zone).toInstant();

                var list = eventDAO.readBetween(fromInstant, toInstant).stream()
                        .map(e -> EventMapper.toDTO(e, currentUserId))
                        .toList();

                ctx.json(list);
                return;
            }

            var list = eventDAO.readAll().stream()
                    .sorted((a, b) -> a.getStartAt().compareTo(b.getStartAt()))
                    .map(e -> EventMapper.toDTO(e, currentUserId))
                    .toList();
            ctx.json(list);

        } catch (Exception e) {
            logger.error("readAll events failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    private ZoneId resolveZone(String tzParam) {
        if (tzParam == null || tzParam.isBlank()) {
            return ZoneId.of("Europe/Copenhagen");
        }
        try {
            return ZoneId.of(tzParam);
        } catch (Exception ex) {
            return ZoneId.of("Europe/Copenhagen");
        }
    }

    private LocalDate parseDateKeywordOrIso(String value, LocalDate today, int defaultOffsetDays) {
        if (value == null || value.isBlank()) {
            return today.plusDays(defaultOffsetDays);
        }
        switch (value.toLowerCase()) {
            case "today":
                return today;
            case "tomorrow":
                return today.plusDays(1);
            default:
                return LocalDate.parse(value);
        }
    }

    private Long extractUserIdFromToken(Object tokenUser) {
        if (tokenUser instanceof JwtUserDTO ju) {
            var u = userDAO.readByEmail(ju.getUsername());
            return u != null ? u.getId() : null;
        }
        if (tokenUser instanceof com.carebridge.dtos.UserDTO du) {
            return du.getId();
        }
        return null;
    }


    @Override
    public void create(Context ctx) {
        try {
            var dto = ctx.bodyAsClass(EventDTO.class);
            if (dto.getTitle() == null || dto.getTitle().isBlank())
                throw new ApiRuntimeException(400, "title required");
            if (dto.getStartAt() == null) throw new ApiRuntimeException(400, "startAt required");
            if (dto.getStartAt().isBefore(Instant.now()))
                throw new ApiRuntimeException(400, "startAt must be in future");
            if (dto.getEventTypeId() == null) throw new ApiRuntimeException(400, "eventTypeId required");

            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof JwtUserDTO ju) email = ju.getUsername();
            else if (tokenUser instanceof com.carebridge.dtos.UserDTO du) email = du.getEmail();
            else if (tokenUser != null) email = tokenUser.toString();

            if (email == null) throw new ApiRuntimeException(401, "Unauthorized");

            User creator = userDAO.readByEmail(email);
            if (creator == null) throw new ApiRuntimeException(401, "Unauthorized");

            EventType et = eventTypeDAO.read(dto.getEventTypeId());
            if (et == null) throw new ApiRuntimeException(404, "EventType not found");

            Event e = new Event(dto.getTitle(), dto.getDescription(), dto.getStartAt(), dto.isShowOnBoard(), creator, et);
            var created = eventDAO.create(e);
            ctx.status(201).json(EventMapper.toDTO(created));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create event failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = parseId(ctx);
            var dto = ctx.bodyAsClass(EventDTO.class);

            Event patch = new Event();
            patch.setTitle(dto.getTitle());
            patch.setDescription(dto.getDescription());
            patch.setStartAt(dto.getStartAt());
            patch.setShowOnBoard(dto.isShowOnBoard());
            if (dto.getEventTypeId() != null) {
                EventType et = eventTypeDAO.read(dto.getEventTypeId());
                if (et == null) throw new ApiRuntimeException(404, "EventType not found");
                patch.setEventType(et);
            }
            var updated = eventDAO.update(id, patch);
            if (updated == null) {
                ctx.status(404).json("{\"msg\":\"Event not found\"}");
                return;
            }
            ctx.json(EventMapper.toDTO(updated));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("update event failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx);
            eventDAO.delete(id);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("delete event failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void readByCreator(Context ctx) {
        try {
            Long userId = Long.parseLong(ctx.pathParam("userId"));
            var list = eventDAO.readByCreator(userId).stream()
                    .map(EventMapper::toDTO)
                    .collect(Collectors.toList());
            ctx.json(list);
        } catch (NumberFormatException ex) {
            ctx.status(400).json("{\"msg\":\"Invalid userId\"}");
        } catch (Exception e) {
            logger.error("readByCreator failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void readUpcoming(Context ctx) {
        try {
            var now = Instant.now();
            var list = eventDAO.readAll().stream()
                    .filter(e -> e.getStartAt() != null && !e.getStartAt().isBefore(now))
                    .map(EventMapper::toDTO)
                    .collect(Collectors.toList());
            ctx.json(list);
        } catch (Exception e) {
            logger.error("readUpcoming failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void markSeen(Context ctx) {
        try {
            Long eventId = parseId(ctx);

            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof JwtUserDTO ju) {
                email = ju.getUsername();
            } else if (tokenUser instanceof com.carebridge.dtos.UserDTO du) {
                email = du.getEmail();
            }

            if (email == null) throw new ApiRuntimeException(401, "Unauthorized");

            var user = userDAO.readByEmail(email);
            if (user == null) throw new ApiRuntimeException(401, "Unauthorized");

            eventDAO.addSeenByUser(eventId, user);
            ctx.status(204);

        } catch (ApiRuntimeException e) {
            ctx.status(e.getErrorCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("markSeen failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public Event validateEntity(Context ctx) {
        return ctx.bodyAsClass(Event.class);
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (Exception ex) {
            throw new ApiRuntimeException(400, "Invalid id");
        }
    }
}
