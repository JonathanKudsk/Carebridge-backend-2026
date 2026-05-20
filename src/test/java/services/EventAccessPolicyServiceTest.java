package services;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dtos.EventDTO;
import com.carebridge.entities.Event;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.enums.EventAccessLevel;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.services.EventAccessPolicyService;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventAccessPolicyServiceTest {

    private EventAccessPolicyService policyService;
    private UserDAO userDAO;

    @BeforeAll
    void setupClass() {
        HibernateConfig.setTest(true);
        HibernateConfig.getEntityManagerFactoryForTest();
        policyService = new EventAccessPolicyService();
        userDAO = UserDAO.getInstance();
    }

    private User createUser(String email, Role role) {
        User u = new User();
        u.setName("Policy Test User");
        u.setEmail(email);
        u.setRole(role);
        u.setPassword("pass123");
        return userDAO.create(u);
    }

    private Event buildEvent(EventAccessLevel accessLevel, Integer riskLevel, Set<User> usersWithAccess) {
        Event event = new Event();
        event.setTitle("Policy Test Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setAccessLevel(accessLevel);
        event.setRiskLevel(riskLevel);
        event.setUsersWithAccess(usersWithAccess != null ? usersWithAccess : Set.of());
        return event;
    }

    // --- resolveDefaultRiskLevel ---

    @Test
    void resolveDefaultRiskLevel_id1_returns1() {
        assertEquals(1, policyService.resolveDefaultRiskLevel(1L));
    }

    @Test
    void resolveDefaultRiskLevel_id3_returns2() {
        assertEquals(2, policyService.resolveDefaultRiskLevel(3L));
    }

    @Test
    void resolveDefaultRiskLevel_id7_returns4() {
        assertEquals(4, policyService.resolveDefaultRiskLevel(7L));
    }

    @Test
    void resolveDefaultRiskLevel_id10_returns5() {
        assertEquals(5, policyService.resolveDefaultRiskLevel(10L));
    }

    @Test
    void resolveDefaultRiskLevel_nullId_throws400() {
        ApiRuntimeException ex = assertThrows(ApiRuntimeException.class,
                () -> policyService.resolveDefaultRiskLevel(null));
        assertEquals(400, ex.getErrorCode());
    }

    // --- resolveDefaultAccessLevel ---

    @Test
    void resolveDefaultAccessLevel_riskLevel1_returnsRoleBased() {
        assertEquals(EventAccessLevel.ROLE_BASED, policyService.resolveDefaultAccessLevel(1));
    }

    @Test
    void resolveDefaultAccessLevel_riskLevel3_returnsRoleBased() {
        assertEquals(EventAccessLevel.ROLE_BASED, policyService.resolveDefaultAccessLevel(3));
    }

    @Test
    void resolveDefaultAccessLevel_riskLevel4_returnsPrivateCreatorOnly() {
        assertEquals(EventAccessLevel.PRIVATE_CREATOR_ONLY, policyService.resolveDefaultAccessLevel(4));
    }

    @Test
    void resolveDefaultAccessLevel_riskLevel5_returnsPrivateCreatorOnly() {
        assertEquals(EventAccessLevel.PRIVATE_CREATOR_ONLY, policyService.resolveDefaultAccessLevel(5));
    }

    @Test
    void resolveDefaultAccessLevel_invalidLevel_throws400() {
        ApiRuntimeException ex = assertThrows(ApiRuntimeException.class,
                () -> policyService.resolveDefaultAccessLevel(6));
        assertEquals(400, ex.getErrorCode());
    }

    @Test
    void resolveDefaultAccessLevel_nullLevel_throws400() {
        ApiRuntimeException ex = assertThrows(ApiRuntimeException.class,
                () -> policyService.resolveDefaultAccessLevel(null));
        assertEquals(400, ex.getErrorCode());
    }

    // --- resolveRiskColor ---

    @Test
    void resolveRiskColor_allLevels() {
        assertEquals("grøn",   policyService.resolveRiskColor(1));
        assertEquals("gul",    policyService.resolveRiskColor(2));
        assertEquals("orange", policyService.resolveRiskColor(3));
        assertEquals("rød",    policyService.resolveRiskColor(4));
        assertEquals("lilla",  policyService.resolveRiskColor(5));
    }

    @Test
    void resolveRiskColor_invalidLevel_throws400() {
        ApiRuntimeException ex = assertThrows(ApiRuntimeException.class,
                () -> policyService.resolveRiskColor(0));
        assertEquals(400, ex.getErrorCode());
    }

    // --- resolveRiskDescription ---

    @Test
    void resolveRiskDescription_allLevels() {
        assertEquals("Ingen risiko",    policyService.resolveRiskDescription(1));
        assertEquals("Lav risiko",      policyService.resolveRiskDescription(2));
        assertEquals("Moderat risiko",  policyService.resolveRiskDescription(3));
        assertEquals("Høj risiko",      policyService.resolveRiskDescription(4));
        assertEquals("Kritisk risiko",  policyService.resolveRiskDescription(5));
    }

    @Test
    void resolveRiskDescription_invalidLevel_throws400() {
        ApiRuntimeException ex = assertThrows(ApiRuntimeException.class,
                () -> policyService.resolveRiskDescription(6));
        assertEquals(400, ex.getErrorCode());
    }

    // --- validateAccessInput ---

    @Test
    void validateAccessInput_missingResidentId_throws400() {
        EventDTO dto = new EventDTO();
        dto.setEventTypeId(1L);
        dto.setResidentId(null);

        ApiRuntimeException ex = assertThrows(ApiRuntimeException.class,
                () -> policyService.validateAccessInput(dto));
        assertEquals(400, ex.getErrorCode());
    }

    @Test
    void validateAccessInput_validInput_doesNotThrow() {
        EventDTO dto = new EventDTO();
        dto.setResidentId(1L);
        dto.setEventTypeId(3L);

        assertDoesNotThrow(() -> policyService.validateAccessInput(dto));
    }

    // --- canUserAccessEvent ---

    @Test
    void canUserAccessEvent_userInAccessList_returnsTrue() {
        User user = createUser("can_access_" + System.currentTimeMillis() + "@test.com", Role.USER);

        Event event = buildEvent(EventAccessLevel.PRIVATE_CREATOR_ONLY, 5, Set.of(user));

        assertTrue(policyService.canUserAccessEvent(user.getId(), Set.of(Role.USER), event));

        userDAO.delete(user.getId());
    }

    @Test
    void canUserAccessEvent_privateEvent_userNotInList_returnsFalse() {
        Event event = buildEvent(EventAccessLevel.PRIVATE_CREATOR_ONLY, 5, Set.of());

        assertFalse(policyService.canUserAccessEvent(999L, Set.of(Role.USER), event));
    }

    @Test
    void canUserAccessEvent_roleBased_sufficientLevel_returnsTrue() {
        // CAREWORKER = 3, riskLevel = 3 → 3 >= 3 → true
        Event event = buildEvent(EventAccessLevel.ROLE_BASED, 3, Set.of());

        assertTrue(policyService.canUserAccessEvent(999L, Set.of(Role.CAREWORKER), event));
    }

    @Test
    void canUserAccessEvent_roleBased_insufficientLevel_returnsFalse() {
        // CAREWORKER = 3, riskLevel = 4 → 3 < 4 → false
        Event event = buildEvent(EventAccessLevel.ROLE_BASED, 4, Set.of());

        assertFalse(policyService.canUserAccessEvent(999L, Set.of(Role.CAREWORKER), event));
    }

    @Test
    void canUserAccessEvent_roleBased_adminAlwaysAccess() {
        // ADMIN = 5 >= any riskLevel
        Event event = buildEvent(EventAccessLevel.ROLE_BASED, 5, Set.of());

        assertTrue(policyService.canUserAccessEvent(999L, Set.of(Role.ADMIN), event));
    }

    // --- resolveEffectiveAccess ---

    @Test
    void resolveEffectiveAccess_userInAccessList_returnsTrue() {
        User user = createUser("effective_" + System.currentTimeMillis() + "@test.com", Role.USER);

        Event event = buildEvent(EventAccessLevel.PRIVATE_CREATOR_ONLY, 5, Set.of(user));

        assertTrue(policyService.resolveEffectiveAccess(event, user.getId(), 1));

        userDAO.delete(user.getId());
    }

    @Test
    void resolveEffectiveAccess_roleBased_sufficientLevel_returnsTrue() {
        Event event = buildEvent(EventAccessLevel.ROLE_BASED, 2, Set.of());

        // userAccessLevel 3 >= riskLevel 2
        assertTrue(policyService.resolveEffectiveAccess(event, 999L, 3));
    }

    @Test
    void resolveEffectiveAccess_roleBased_insufficientLevel_returnsFalse() {
        Event event = buildEvent(EventAccessLevel.ROLE_BASED, 3, Set.of());

        // userAccessLevel 1 < riskLevel 3
        assertFalse(policyService.resolveEffectiveAccess(event, 999L, 1));
    }

    @Test
    void resolveEffectiveAccess_privateEvent_noDirectAccess_returnsFalse() {
        Event event = buildEvent(EventAccessLevel.PRIVATE_CREATOR_ONLY, 5, Set.of());

        assertFalse(policyService.resolveEffectiveAccess(event, 999L, 5));
    }

    // --- resolveUsersWithAccess ---

    @Test
    void resolveUsersWithAccess_privateCreatorOnly_containsCreatorResidentAndCustom() {
        Set<Long> result = policyService.resolveUsersWithAccess(
                EventAccessLevel.PRIVATE_CREATOR_ONLY,
                1L, 2L, List.of(3L), 5, false);

        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        assertTrue(result.contains(3L));
    }

    @Test
    void resolveUsersWithAccess_riskLevel5_guardianNotAdded() {
        // riskLevel=5 → guardian should NOT be auto-added
        Set<Long> result = policyService.resolveUsersWithAccess(
                EventAccessLevel.PRIVATE_CREATOR_ONLY,
                1L, 2L, null, 5, false);

        assertEquals(2, result.size()); // only creator + resident
    }

    @Test
    void resolveUsersWithAccess_isPrivateTrue_overridesRoleBased() {
        // Even if level=ROLE_BASED, isPrivate=true should use private logic
        Set<Long> result = policyService.resolveUsersWithAccess(
                EventAccessLevel.ROLE_BASED,
                10L, 20L, null, 5, true);

        assertTrue(result.contains(10L));
        assertTrue(result.contains(20L));
    }

    @Test
    void resolveUsersWithAccess_roleBased_includesUsersWithSufficientRole() {
        User admin = createUser("policy_admin_" + System.currentTimeMillis() + "@test.com", Role.ADMIN);
        User regularUser = createUser("policy_user_" + System.currentTimeMillis() + "@test.com", Role.USER);

        // riskLevel=4: ADMIN(5) >= 4 → included. USER(1) < 4 → excluded.
        Set<Long> result = policyService.resolveUsersWithAccess(
                EventAccessLevel.ROLE_BASED,
                99L, 999L, null, 4, false);

        assertTrue(result.contains(admin.getId()));
        assertFalse(result.contains(regularUser.getId()));

        userDAO.delete(admin.getId());
        userDAO.delete(regularUser.getId());
    }
}
