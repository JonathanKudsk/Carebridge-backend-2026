package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.CityDAO;
import com.carebridge.entities.City;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import populator.LocationPopulator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class CityDAOTest {
    static private EntityManagerFactory emf;
    static private CityDAO dao;


    @BeforeAll
    static void setupOnce() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactory();
        dao = CityDAO.getInstance();
    }

    @BeforeEach
    void setup() {
        LocationPopulator.populate();
    }

    @AfterEach
        //delete all data
    void teardown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        //delete everything
        em.createNativeQuery("TRUNCATE TABLE cities, locations RESTART IDENTITY CASCADE")
                .executeUpdate();
        em.getTransaction().commit();

        em.close();
    }

    @Test
    public void testCreateCity() {
        City Expected = City.builder().id(3L).cityname("created").postalcode(1234).build();
        City created = City.builder().cityname("created").postalcode(1234).build();
        City actual = dao.create(created);
        assertNotNull(created);
        assertEquals(Expected,created);
        assertEquals(Expected,actual);
    }

    @Test
    public void testReadCity() {
        City Expected = City.builder().id(1L).cityname("By1").postalcode(1111).build();
        City read = dao.read(1L);
        assertEquals(Expected, read);
    }

    @Test
    public void testReadCity2() {
        City expected = City.builder().id(1L).cityname("By1").postalcode(1111).build();
        City read = dao.read(expected.getPostalcode(), expected.getCityname());
        assertEquals(expected, read);
    }

    @Test
    public void testUpdateCity() {
        City expected = City.builder().id(1L).cityname("By1").postalcode(1112).build();
        City updated = dao.update(expected.getId(), expected);
        assertEquals(expected,updated);
    }

    @Test
    public void testReadAllCities() {
        List<City> all = dao.readAll();
        assertEquals(all.size(), 2);
    }

    @Test
    public void testDeleteCity() {
        dao.delete(1L);
        assertNull(dao.read(1L));
    }
}