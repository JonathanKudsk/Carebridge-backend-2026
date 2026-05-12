package com.carebridge.services;

import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.EventDTO;
import com.carebridge.entities.Event;
import com.carebridge.entities.enums.Role;
import com.carebridge.enums.EventAccessLevel;
import com.carebridge.exceptions.ApiRuntimeException;

import java.util.*;

public class EventAccessPolicyService {

    private final UserDAO userDAO;

    private static final Map<Long, Integer> EVENT_TYPE_RISK_MAP = Map.of(
        1L, 1,
        2L, 1,
        3L, 2,
        4L, 2,
        5L, 3,
        6L, 3,
        7L, 4,
        8L, 4,
        9L, 5,
        10L, 5
    );

    private static final Map<Role, Integer> ROLE_ACCESS_LEVELS = Map.of(
        Role.USER, 1,
        Role.CAREWORKER, 3,
        Role.GUARDIAN, 4,
        Role.ADMIN, 5
    );

    public EventAccessPolicyService() {
        this.userDAO = UserDAO.getInstance();
    }

    public Integer resolveDefaultRiskLevel(Long eventTypeId) {
        Integer riskLevel = EVENT_TYPE_RISK_MAP.get(eventTypeId);
        if (riskLevel == null) {
            throw new ApiRuntimeException(400, "Unknown eventTypeId: " + eventTypeId);
        }
        return riskLevel;
    }

    // Risk 1-3 → role-based open access. Risk 4-5 → private by default.
    public EventAccessLevel resolveDefaultAccessLevel(Integer riskLevel) {
        if (riskLevel == null || riskLevel < 1 || riskLevel > 5) {
            throw new ApiRuntimeException(400, "Invalid risk level: " + riskLevel);
        }
        return (riskLevel >= 4) ? EventAccessLevel.PRIVATE_CREATOR_ONLY : EventAccessLevel.ROLE_BASED;
    }

    public String resolveRiskColor(Integer riskLevel) {
        return switch (riskLevel) {
            case 1 -> "grøn";
            case 2 -> "gul";
            case 3 -> "orange";
            case 4 -> "rød";
            case 5 -> "lilla";
            default -> throw new ApiRuntimeException(400, "Invalid risk level: " + riskLevel);
        };
    }

    public String resolveRiskDescription(Integer riskLevel) {
        return switch (riskLevel) {
            case 1 -> "Ingen risiko";
            case 2 -> "Lav risiko";
            case 3 -> "Moderat risiko";
            case 4 -> "Høj risiko";
            case 5 -> "Kritisk risiko";
            default -> throw new ApiRuntimeException(400, "Invalid risk level: " + riskLevel);
        };
    }

    public Set<Long> resolveUsersWithAccess(EventAccessLevel level, Long creatorId, Long residentId,
                                            List<Long> customUserIds, Integer riskLevel, Boolean isPrivate) {
        Set<Long> result = new HashSet<>();

        boolean effectivelyPrivate = Boolean.TRUE.equals(isPrivate) || level == EventAccessLevel.PRIVATE_CREATOR_ONLY;

        if (effectivelyPrivate) {
            result.add(creatorId);
            result.add(residentId);
            if (customUserIds != null) result.addAll(customUserIds);
        } else {
            for (Map.Entry<Role, Integer> entry : ROLE_ACCESS_LEVELS.entrySet()) {
                if (entry.getValue() >= riskLevel) {
                    result.addAll(userDAO.findUserIdsByRole(entry.getKey()));
                }
            }
            if (customUserIds != null) result.addAll(customUserIds);
        }

        // Guardian auto-add for both private and non-private, except at risk level 5.
        if (riskLevel == null || riskLevel < 5) {
            userDAO.findGuardianIdByResidentId(residentId).ifPresent(result::add);
        }

        return result;
    }

    public void validateAccessInput(EventDTO dto) {
        if (dto.getResidentId() == null) {
            throw new ApiRuntimeException(400, "residentId must not be null");
        }
        resolveDefaultRiskLevel(dto.getEventTypeId());
    }

    public Boolean canUserAccessEvent(Long userId, Set<Role> roles, Event event) {
        boolean directAccess = event.getUsersWithAccess().stream()
            .anyMatch(u -> u.getId().equals(userId));
        if (directAccess) return true;

        if (event.getAccessLevel() != EventAccessLevel.ROLE_BASED) return false;

        int eventRiskLevel = event.getRiskLevel() != null ? event.getRiskLevel() : 1;
        int userMaxLevel = roles.stream()
            .mapToInt(r -> ROLE_ACCESS_LEVELS.getOrDefault(r, 0))
            .max()
            .orElse(0);
        return userMaxLevel >= eventRiskLevel;
    }

    public Boolean resolveEffectiveAccess(Event event, Long userId, Integer userAccessLevel) {
        boolean directAccess = event.getUsersWithAccess().stream()
            .anyMatch(u -> u.getId().equals(userId));
        if (directAccess) return true;

        if (event.getAccessLevel() == EventAccessLevel.ROLE_BASED) {
            int eventRiskLevel = event.getRiskLevel() != null ? event.getRiskLevel() : 1;
            return userAccessLevel >= eventRiskLevel;
        }

        return false;
    }
}
