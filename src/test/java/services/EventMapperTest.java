package services;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.EventDTO;
import com.carebridge.entities.Event;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.enums.EventAccessLevel;
import com.carebridge.services.mappers.EventMapper;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventMapperTest {

    private UserDAO userDAO;

    @BeforeAll
    void setupClass() {
        HibernateConfig.setTest(true);
        HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = UserDAO.getInstance();
    }

    private User createUser(String email, Role role) {
        User u = new User();
        u.setName("Mapper Test User");
        u.setEmail(email);
        u.setRole(role);
        u.setPassword("pass123");
        return userDAO.create(u);
    }

    private Event buildEvent(String title) {
        Event event = new Event();
        event.setTitle(title);
        event.setStartAt(Instant.now().plusSeconds(3600));
        return event;
    }

    // --- toDTO ---

    @Test
    void toDTO_withNullEvent_returnsNull() {
        assertNull(EventMapper.toDTO(null));
    }

    @Test
    void toDTO_mapsBasicAccessFields() {
        Event event = buildEvent("Basic Fields");
        event.setRiskLevel(2);
        event.setRiskColor("gul");
        event.setRiskDescription("Lav risiko");
        event.setResidentId(42L);
        event.setAccessLevel(EventAccessLevel.ROLE_BASED);

        EventDTO dto = EventMapper.toDTO(event);

        assertEquals(2, dto.getRiskLevel());
        assertEquals("gul", dto.getRiskColor());
        assertEquals("Lav risiko", dto.getRiskDescription());
        assertEquals(42L, dto.getResidentId());
        assertEquals("ROLE_BASED", dto.getAccessLevel());
    }

    @Test
    void toDTO_accessLevel_mappedAsEnumName() {
        Event event = buildEvent("AccessLevel Mapping");
        event.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);

        EventDTO dto = EventMapper.toDTO(event);

        assertEquals("PRIVATE_CREATOR_ONLY", dto.getAccessLevel());
    }

    @Test
    void toDTO_nullAccessLevel_returnsNullInDto() {
        Event event = buildEvent("Null AccessLevel");
        event.setAccessLevel(null);

        EventDTO dto = EventMapper.toDTO(event);

        assertNull(dto.getAccessLevel());
    }

    @Test
    void toDTO_isPrivate_alwaysNullInResponse() {
        Event event = buildEvent("isPrivate Check");
        event.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);

        EventDTO dto = EventMapper.toDTO(event);

        assertNull(dto.getIsPrivate());
    }

    @Test
    void toDTO_emptyUsersWithAccess_returnsEmptyList() {
        Event event = buildEvent("No Access Users");
        event.setUsersWithAccess(Set.of());

        EventDTO dto = EventMapper.toDTO(event);

        assertNotNull(dto.getUsersWithAccessIds());
        assertTrue(dto.getUsersWithAccessIds().isEmpty());
    }

    @Test
    void toDTO_withUsersWithAccess_mapsIds() {
        User user = createUser("mapper_access_" + System.currentTimeMillis() + "@test.com", Role.USER);

        Event event = buildEvent("Access Users Event");
        event.setUsersWithAccess(Set.of(user));

        EventDTO dto = EventMapper.toDTO(event);

        assertTrue(dto.getUsersWithAccessIds().contains(user.getId()));

        userDAO.delete(user.getId());
    }

    @Test
    void toDTO_withCurrentUserId_matchesSeenByUsers_setsTrue() {
        User user = createUser("mapper_seen_" + System.currentTimeMillis() + "@test.com", Role.USER);

        Event event = buildEvent("Seen Event");
        event.setSeenByUsers(Set.of(user));

        EventDTO dto = EventMapper.toDTO(event, user.getId());

        assertTrue(dto.isSeenByCurrentUser());

        userDAO.delete(user.getId());
    }

    @Test
    void toDTO_withCurrentUserId_notInSeenByUsers_setsFalse() {
        Event event = buildEvent("Not Seen Event");
        event.setSeenByUsers(Set.of());

        EventDTO dto = EventMapper.toDTO(event, 999L);

        assertFalse(dto.isSeenByCurrentUser());
    }

    // --- toEntity ---

    @Test
    void toEntity_withNullDto_returnsNull() {
        assertNull(EventMapper.toEntity(null, null, null, null));
    }

    @Test
    void toEntity_mapsAccessFields() {
        EventDTO dto = new EventDTO();
        dto.setTitle("Entity Mapping");
        dto.setResidentId(10L);
        dto.setRiskLevel(3);
        dto.setRiskColor("orange");
        dto.setRiskDescription("Moderat risiko");
        dto.setAccessLevel("ROLE_BASED");

        Event entity = EventMapper.toEntity(dto, null, null, null);

        assertEquals(10L, entity.getResidentId());
        assertEquals(3, entity.getRiskLevel());
        assertEquals("orange", entity.getRiskColor());
        assertEquals("Moderat risiko", entity.getRiskDescription());
        assertEquals(EventAccessLevel.ROLE_BASED, entity.getAccessLevel());
    }

    @Test
    void toEntity_nullAccessLevel_defaultsToRoleBased() {
        EventDTO dto = new EventDTO();
        dto.setTitle("Null AL");
        dto.setAccessLevel(null);

        Event entity = EventMapper.toEntity(dto, null, null, null);

        assertEquals(EventAccessLevel.ROLE_BASED, entity.getAccessLevel());
    }

    @Test
    void toEntity_invalidAccessLevel_defaultsToRoleBased() {
        EventDTO dto = new EventDTO();
        dto.setTitle("Invalid AL");
        dto.setAccessLevel("NOT_VALID");

        Event entity = EventMapper.toEntity(dto, null, null, null);

        assertEquals(EventAccessLevel.ROLE_BASED, entity.getAccessLevel());
    }

    @Test
    void toEntity_withUsersWithAccess_setsRelation() {
        User user = createUser("mapper_entity_" + System.currentTimeMillis() + "@test.com", Role.CAREWORKER);

        EventDTO dto = new EventDTO();
        dto.setTitle("Entity With Access");

        Event entity = EventMapper.toEntity(dto, null, null, Set.of(user));

        assertTrue(entity.getUsersWithAccess().contains(user));

        userDAO.delete(user.getId());
    }

    @Test
    void toEntity_nullUsersWithAccess_setsEmptySet() {
        EventDTO dto = new EventDTO();
        dto.setTitle("Null Users");

        Event entity = EventMapper.toEntity(dto, null, null, null);

        assertNotNull(entity.getUsersWithAccess());
        assertTrue(entity.getUsersWithAccess().isEmpty());
    }

    // --- updateEntityFromDTO ---

    @Test
    void updateEntityFromDTO_updatesAccessFields() {
        Event event = buildEvent("Original");
        event.setRiskLevel(1);
        event.setAccessLevel(EventAccessLevel.ROLE_BASED);

        EventDTO dto = new EventDTO();
        dto.setTitle("Updated");
        dto.setRiskLevel(3);
        dto.setRiskColor("orange");
        dto.setRiskDescription("Moderat risiko");
        dto.setResidentId(5L);
        dto.setAccessLevel("PRIVATE_CREATOR_ONLY");

        Event updated = EventMapper.updateEntityFromDTO(event, dto, null, Set.of());

        assertEquals("Updated", updated.getTitle());
        assertEquals(3, updated.getRiskLevel());
        assertEquals("orange", updated.getRiskColor());
        assertEquals(5L, updated.getResidentId());
        assertEquals(EventAccessLevel.PRIVATE_CREATOR_ONLY, updated.getAccessLevel());
    }

    @Test
    void updateEntityFromDTO_withNullEvent_returnsNull() {
        assertNull(EventMapper.updateEntityFromDTO(null, new EventDTO(), null, null));
    }

    @Test
    void updateEntityFromDTO_withNullDto_returnsOriginalEvent() {
        Event event = buildEvent("Keep Title");
        Event result = EventMapper.updateEntityFromDTO(event, null, null, null);
        assertEquals(event, result);
    }
}
