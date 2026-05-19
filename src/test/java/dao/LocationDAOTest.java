package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.LocationDAO;
import com.carebridge.entities.Location;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import populator.LocationPopulator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class LocationDAOTest {
    static private EntityManagerFactory emf;
    static private LocationDAO dao;


    @BeforeAll
    static void setupOnce() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactory();
        dao = LocationDAO.getInstance();
    }

    @BeforeEach
    void setup() {
        clearTables();
        LocationPopulator.populate();
    }

    @AfterEach
        //delete all data
    void teardown() {
        clearTables();
    }

    private void clearTables() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        //delete everything
        em.createNativeQuery("TRUNCATE TABLE cities, locations RESTART IDENTITY CASCADE")
                .executeUpdate();
        em.getTransaction().commit();

        em.close();
    }

    @Test
    public void testCreateLocation() {
        Location Expected = Location.builder().id(4L).locationName("created").address("test").build();
        Location created = Location.builder().locationName("created").address("test").build();
        Location actual = dao.create(created);
        assertNotNull(created);
        assertEquals(Expected,created);
        assertEquals(Expected,actual);
    }

    @Test
    public void testReadLocation() {
        Location Expected = LocationPopulator.fetch().get(0);
        Location read = dao.read(1L);
        assertEquals(Expected, read);
    }

    @Test
    public void testUpdateLocation() {
        Location expected = LocationPopulator.fetch().get(0);
        expected.setLocationName("something else");
        Location updated = dao.update(expected.getId(), expected);
        assertEquals(expected,updated);
        assertEquals(expected,dao.read(expected.getId()));
    }

    @Test
    public void testReadAllLocations() {
        List<Location> all = dao.readAll();
        assertEquals(all.size(), 3);
    }

    @Test
    public void testDeleteCity() {
        dao.delete(1L);
        assertNull(dao.read(1L));
    }
}
