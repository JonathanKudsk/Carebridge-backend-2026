package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.entities.EventType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventTypeDAOTest {

    private EventTypeDAO eventTypeDAO;
    private EventType testEventType;

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        EntityManagerFactory emfTest = HibernateConfig.getEntityManagerFactoryForTest();
        eventTypeDAO = EventTypeDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
        testEventType = new EventType();
        testEventType.setName("Shared EventType");
        testEventType.setColorHex("#123456");
        eventTypeDAO.create(testEventType);
    }

    @AfterEach
    public void cleanup() {
        if (testEventType != null) {
            try {
                eventTypeDAO.delete(testEventType.getId());
            } catch (Exception ignored) {}
        }
    }

    @Test
    public void testCreateEventType() {
        EventType type = new EventType();
        type.setName("Create Type");
        type.setColorHex("#123456");
        EventType created = eventTypeDAO.create(type);
        assertNotNull(created.getId());

        eventTypeDAO.delete(created.getId());
    }

    @Test
    public void testReadEventType() {
        EventType read = eventTypeDAO.read(testEventType.getId());
        assertEquals("Shared EventType", read.getName());
    }

    @Test
    public void testUpdateEventType() {
        testEventType.setColorHex("#BBBBBB");
        EventType updated = eventTypeDAO.update(testEventType.getId(), testEventType);
        assertEquals("#BBBBBB", updated.getColorHex());
    }

    @Test
    public void testReadByName() {
        EventType read = eventTypeDAO.readByName("Shared EventType");
        assertNotNull(read);
        assertEquals(testEventType.getId(), read.getId());
    }

    @Test
    public void testReadAllEventTypes() {
        List<EventType> all = eventTypeDAO.readAll();
        assertTrue(all.size() >= 1);
    }

    @Test
    public void testDeleteEventType() {
        EventType type = new EventType();
        type.setName("Delete Type");
        type.setColorHex("#000000");
        EventType created = eventTypeDAO.create(type);

        eventTypeDAO.delete(created.getId());
        assertNull(eventTypeDAO.read(created.getId()));
    }
}

