package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.EventDAO;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.enums.EventAccessLevel;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventDAOTest {

    private EventDAO eventDAO;
    private UserDAO userDAO;
    private EventTypeDAO eventTypeDAO;

    private User testUser;
    private EventType testEventType;

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        EntityManagerFactory emfTest = HibernateConfig.getEntityManagerFactoryForTest();

        eventDAO = EventDAO.getInstance();
        userDAO = UserDAO.getInstance();
        eventTypeDAO = EventTypeDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
        // Delt bruger og eventType for hver test
        testUser = new User();
        testUser.setName("Event User");
        testUser.setEmail("eventuser@example.com");
        testUser.setRole(Role.USER);
        testUser.setPassword("test123");
        userDAO.create(testUser);

        testEventType = new EventType();
        testEventType.setName("Event Type");
        testEventType.setColorHex("#123456");
        eventTypeDAO.create(testEventType);
    }

    @AfterEach
    public void cleanup() {
        if (testEventType != null) eventTypeDAO.delete(testEventType.getId());
        if (testUser != null) userDAO.delete(testUser.getId());
    }

    @Test
    public void testCreateEvent() {
        Event event = new Event();
        event.setTitle("Create Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setShowOnBoard(true);

        Event created = eventDAO.create(event);
        assertNotNull(created.getId());

        eventDAO.delete(created.getId());
    }

    @Test
    public void testReadEvent() {
        Event event = new Event();
        event.setTitle("Read Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setShowOnBoard(true);

        Event created = eventDAO.create(event);
        Event read = eventDAO.read(created.getId());
        assertEquals("Read Event", read.getTitle());

        eventDAO.delete(created.getId());
    }

    @Test
    public void testUpdateEvent() {
        Event event = new Event();
        event.setTitle("Update Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setShowOnBoard(true);

        Event created = eventDAO.create(event);
        created.setTitle("Updated Event");
        Event updated = eventDAO.update(created.getId(), created);
        assertEquals("Updated Event", updated.getTitle());

        eventDAO.delete(created.getId());
    }

    @Test
    public void testDeleteEvent() {
        Event event = new Event();
        event.setTitle("Delete Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setShowOnBoard(true);

        Event created = eventDAO.create(event);
        eventDAO.delete(created.getId());
        assertNull(eventDAO.read(created.getId()));
    }

    @Test
    public void testReadAllEvents() {
        Event event1 = new Event();
        event1.setTitle("Event 1");
        event1.setStartAt(Instant.now().plusSeconds(3600));
        event1.setCreatedBy(testUser);
        event1.setEventType(testEventType);
        event1.setShowOnBoard(true);
        eventDAO.create(event1);

        Event event2 = new Event();
        event2.setTitle("Event 2");
        event2.setStartAt(Instant.now().plusSeconds(7200));
        event2.setCreatedBy(testUser);
        event2.setEventType(testEventType);
        event2.setShowOnBoard(true);
        eventDAO.create(event2);

        List<Event> events = eventDAO.readAll();
        assertTrue(events.size() >= 2);

        eventDAO.delete(event1.getId());
        eventDAO.delete(event2.getId());
    }

    @Test
    public void testReadByCreator() {
        Event event = new Event();
        event.setTitle("Creator Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setShowOnBoard(true);

        Event created = eventDAO.create(event);
        List<Event> byCreator = eventDAO.readByCreator(testUser.getId());
        assertTrue(byCreator.stream().anyMatch(e -> e.getId().equals(created.getId())));

        eventDAO.delete(created.getId());
    }

    @Test
    public void testReadAccessibleEvents_includesDirectAccessUser() {
        User accessUser = new User();
        accessUser.setName("Access User");
        accessUser.setEmail("accessuser_direct@example.com");
        accessUser.setRole(Role.USER);
        accessUser.setPassword("test123");
        userDAO.create(accessUser);

        Event event = new Event();
        event.setTitle("Private Direct Access Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);
        event.setRiskLevel(5);
        event.setUsersWithAccess(Set.of(accessUser));
        Event created = eventDAO.create(event);

        List<Event> result = eventDAO.readAccessibleEvents(accessUser.getId());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(created.getId())));

        eventDAO.delete(created.getId());
        userDAO.delete(accessUser.getId());
    }

    @Test
    public void testReadAccessibleEvents_excludesUserWithoutAccess() {
        User otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("otheruser_noaccess@example.com");
        otherUser.setRole(Role.USER);
        otherUser.setPassword("test123");
        userDAO.create(otherUser);

        Event event = new Event();
        event.setTitle("Private No Access Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);
        event.setRiskLevel(5);
        event.setUsersWithAccess(Set.of(testUser));
        Event created = eventDAO.create(event);

        List<Event> result = eventDAO.readAccessibleEvents(otherUser.getId());
        assertFalse(result.stream().anyMatch(e -> e.getId().equals(created.getId())));

        eventDAO.delete(created.getId());
        userDAO.delete(otherUser.getId());
    }

    @Test
    public void testReadAccessibleEvents_roleBasedAccessBySufficientLevel() {
        User careworker = new User();
        careworker.setName("CW User");
        careworker.setEmail("careworker_rolebased@example.com");
        careworker.setRole(Role.CAREWORKER);
        careworker.setPassword("test123");
        userDAO.create(careworker);

        Event event = new Event();
        event.setTitle("Role Based Level 3 Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setAccessLevel(EventAccessLevel.ROLE_BASED);
        event.setRiskLevel(3);
        Event created = eventDAO.create(event);

        List<Event> result = eventDAO.readAccessibleEvents(careworker.getId());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(created.getId())));

        eventDAO.delete(created.getId());
        userDAO.delete(careworker.getId());
    }

    @Test
    public void testReadAccessibleEvents_roleBasedDeniedByInsufficientLevel() {
        User careworker = new User();
        careworker.setName("CW Denied User");
        careworker.setEmail("careworker_denied@example.com");
        careworker.setRole(Role.CAREWORKER);
        careworker.setPassword("test123");
        userDAO.create(careworker);

        Event event = new Event();
        event.setTitle("Role Based Level 4 Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setAccessLevel(EventAccessLevel.ROLE_BASED);
        event.setRiskLevel(4);
        Event created = eventDAO.create(event);

        List<Event> result = eventDAO.readAccessibleEvents(careworker.getId());
        assertFalse(result.stream().anyMatch(e -> e.getId().equals(created.getId())));

        eventDAO.delete(created.getId());
        userDAO.delete(careworker.getId());
    }

    @Test
    public void testReadAccessibleById_returnsNullWhenNoAccess() {
        User noAccessUser = new User();
        noAccessUser.setName("No Access User");
        noAccessUser.setEmail("noaccess_byid@example.com");
        noAccessUser.setRole(Role.USER);
        noAccessUser.setPassword("test123");
        userDAO.create(noAccessUser);

        Event event = new Event();
        event.setTitle("Private By Id Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);
        event.setRiskLevel(5);
        Event created = eventDAO.create(event);

        Event result = eventDAO.readAccessibleById(created.getId(), noAccessUser.getId(), 1);
        assertNull(result);

        eventDAO.delete(created.getId());
        userDAO.delete(noAccessUser.getId());
    }

    @Test
    public void testReadAccessibleById_returnsEventWhenInAccessList() {
        User accessUser = new User();
        accessUser.setName("Access By Id User");
        accessUser.setEmail("access_byid@example.com");
        accessUser.setRole(Role.USER);
        accessUser.setPassword("test123");
        userDAO.create(accessUser);

        Event event = new Event();
        event.setTitle("Accessible By Id Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testEventType);
        event.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);
        event.setRiskLevel(5);
        event.setUsersWithAccess(Set.of(accessUser));
        Event created = eventDAO.create(event);

        Event result = eventDAO.readAccessibleById(created.getId(), accessUser.getId(), 1);
        assertNotNull(result);
        assertEquals(created.getId(), result.getId());

        eventDAO.delete(created.getId());
        userDAO.delete(accessUser.getId());
    }

    @Test
    public void testReadAccessibleEventsBetween_filtersCorrectly() {
        User accessUser = new User();
        accessUser.setName("Between User");
        accessUser.setEmail("between_user@example.com");
        accessUser.setRole(Role.USER);
        accessUser.setPassword("test123");
        userDAO.create(accessUser);

        Instant base = Instant.now().plusSeconds(3600);

        Event inRange1 = new Event();
        inRange1.setTitle("In Range 1");
        inRange1.setStartAt(base.plusSeconds(100));
        inRange1.setCreatedBy(testUser);
        inRange1.setEventType(testEventType);
        inRange1.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);
        inRange1.setRiskLevel(5);
        inRange1.setUsersWithAccess(Set.of(accessUser));
        eventDAO.create(inRange1);

        Event inRange2 = new Event();
        inRange2.setTitle("In Range 2");
        inRange2.setStartAt(base.plusSeconds(200));
        inRange2.setCreatedBy(testUser);
        inRange2.setEventType(testEventType);
        inRange2.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);
        inRange2.setRiskLevel(5);
        inRange2.setUsersWithAccess(Set.of(accessUser));
        eventDAO.create(inRange2);

        Event outOfRange = new Event();
        outOfRange.setTitle("Out Of Range");
        outOfRange.setStartAt(base.plusSeconds(86400));
        outOfRange.setCreatedBy(testUser);
        outOfRange.setEventType(testEventType);
        outOfRange.setAccessLevel(EventAccessLevel.PRIVATE_CREATOR_ONLY);
        outOfRange.setRiskLevel(5);
        outOfRange.setUsersWithAccess(Set.of(accessUser));
        eventDAO.create(outOfRange);

        Instant from = base;
        Instant to = base.plusSeconds(1000);

        List<Event> result = eventDAO.readAccessibleEventsBetween(accessUser.getId(), from, to);
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(inRange1.getId())));
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(inRange2.getId())));
        assertFalse(result.stream().anyMatch(e -> e.getId().equals(outOfRange.getId())));

        eventDAO.delete(inRange1.getId());
        eventDAO.delete(inRange2.getId());
        eventDAO.delete(outOfRange.getId());
        userDAO.delete(accessUser.getId());
    }
}
