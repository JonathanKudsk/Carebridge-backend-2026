package com.carebridge.services;

import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.EventDTO;
import com.carebridge.entities.Event;
import com.carebridge.entities.enums.EventAccessLevel;
import com.carebridge.entities.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class EventAccessPolicyService {

    private final UserDAO userDAO;

    // Event type ID → risk level. Two event types per category, IDs 1–10.
    private static final Map<Long, Integer> EVENT_TYPE_RISK_MAP = Map.of(
        1L, 1,  // Fællesaktivitet
        2L, 1,  // Pleje
        3L, 2,  // Transport
        4L, 2,  // Praktisk
        5L, 3,  // Sundhed
        6L, 3,  // Medicin
        7L, 4,  // Myndigheder
        8L, 4,  // Psykolog
        9L, 5,  // Akut
        10L, 5  // Juridisk
    );

    // Role → numeric access level used for threshold comparisons.
    private static final Map<Role, Integer> ROLE_ACCESS_LEVELS = Map.of(
        Role.USER, 1,
        Role.CAREWORKER, 3,
        Role.GUARDIAN, 4,
        Role.ADMIN, 5
    );

    public EventAccessPolicyService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Maps an event type ID to its risk level (1–5). Throws 400 for unknown IDs.
    public Integer resolveDefaultRiskLevel(Long eventTypeId) {
        Integer riskLevel = EVENT_TYPE_RISK_MAP.get(eventTypeId);
        if (riskLevel == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Unknown eventTypeId: " + eventTypeId);
        }
        return riskLevel;
    }

    // Maps a numeric risk level (1–5) directly to the corresponding EventAccessLevel value.
    public EventAccessLevel resolveDefaultAccessLevel(Integer riskLevel) {
        return switch (riskLevel) {
            case 1 -> EventAccessLevel.LEVEL_1;
            case 2 -> EventAccessLevel.LEVEL_2;
            case 3 -> EventAccessLevel.LEVEL_3;
            case 4 -> EventAccessLevel.LEVEL_4;
            case 5 -> EventAccessLevel.LEVEL_5;
            default -> throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid risk level: " + riskLevel);
        };
    }

    // Returns the Danish color label for the given access level.
    public String resolveRiskColor(EventAccessLevel accessLevel) {
        return switch (accessLevel) {
            case LEVEL_1 -> "grøn";
            case LEVEL_2 -> "gul";
            case LEVEL_3 -> "orange";
            case LEVEL_4 -> "rød";
            case LEVEL_5 -> "lilla";
        };
    }

    // Returns the Danish risk description for the given access level.
    public String resolveRiskDescription(EventAccessLevel accessLevel) {
        return switch (accessLevel) {
            case LEVEL_1 -> "Ingen risiko";
            case LEVEL_2 -> "Lav risiko";
            case LEVEL_3 -> "Moderat risiko";
            case LEVEL_4 -> "Høj risiko";
            case LEVEL_5 -> "Kritisk risiko";
        };
    }

    // Builds the complete set of user IDs that should have access to an event.
    // Private events use an explicit allowlist only; non-private events auto-include
    // users whose role level meets the threshold, plus the resident's guardian (except at level 5).
    public Set<Long> resolveUsersWithAccess(EventAccessLevel level, Long creatorId, Long residentId,
                                            List<Long> customUserIds, Integer riskLevel, Boolean isPrivate) {
        Set<Long> result = new HashSet<>();

        if (Boolean.TRUE.equals(isPrivate)) {
            result.add(creatorId);
            result.add(residentId);
            if (customUserIds != null) result.addAll(customUserIds);
        } else {
            int threshold = level.getLevel();
            for (Map.Entry<Role, Integer> entry : ROLE_ACCESS_LEVELS.entrySet()) {
                if (entry.getValue() >= threshold) {
                    List<Long> userIds = userDAO.findUserIdsByRole(entry.getKey());
                    result.addAll(userIds);
                }
            }
            if (customUserIds != null) result.addAll(customUserIds);

            // Always include the resident's guardian unless the event is at the highest risk level.
            if (level != EventAccessLevel.LEVEL_5) {
                Optional<Long> guardianId = userDAO.findGuardianIdByResidentId(residentId);
                guardianId.ifPresent(result::add);
            }
        }

        return result;
    }

    // Validates access-related fields on an incoming EventDTO.
    // Defaults accessLevel to LEVEL_1 for non-private events that omit it.
    public void validateAccessInput(EventDTO dto) {
        if (dto.getResidentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "residentId must not be null");
        }
        // Will throw 400 if eventTypeId is not in the known mapping.
        resolveDefaultRiskLevel(dto.getEventTypeId());

        if (!dto.getIsPrivate() && dto.getAccessLevel() == null) {
            dto.setAccessLevel(EventAccessLevel.LEVEL_1);
        }
    }

    // Determines whether a user may access an event.
    // Direct membership in usersWithAccess always grants access.
    // For non-private events the user's highest role level is also compared against the event's level.
    public Boolean canUserAccessEvent(Long userId, Set<Role> roles, Event event) {
        boolean directAccess = event.getUsersWithAccess().stream()
            .anyMatch(u -> u.getId().equals(userId));
        if (directAccess) return true;

        if (event.isPrivate()) return false;

        int eventLevelNum = event.getAccessLevel().getLevel();
        int userMaxLevel = roles.stream()
            .mapToInt(r -> ROLE_ACCESS_LEVELS.getOrDefault(r, 0))
            .max()
            .orElse(0);
        return userMaxLevel >= eventLevelNum;
    }

    // Returns true if the user has effective access, either through direct membership
    // or (for non-private events) by having a sufficient numeric access level.
    public Boolean resolveEffectiveAccess(Event event, Long userId, Integer userAccessLevel) {
        boolean directAccess = event.getUsersWithAccess().stream()
            .anyMatch(u -> u.getId().equals(userId));
        if (directAccess) return true;

        if (!event.isPrivate()) {
            return userAccessLevel >= event.getAccessLevel().getLevel();
        }

        return false;
    }
}
