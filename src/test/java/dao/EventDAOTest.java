package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.EventDAO;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

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
}
