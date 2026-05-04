package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.TemplateDAO;
import com.carebridge.entities.Field;
import com.carebridge.entities.Template;
import com.carebridge.enums.FieldType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.*;
import populator.TemplatePopulator;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class TemplateDaoTest {
    static private EntityManagerFactory emf;
    static private TemplateDAO dao;


    @BeforeAll
    static void setupOnce() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactory();
        dao = TemplateDAO.getInstance();
    }

    @BeforeEach
    void setup(){
        TemplatePopulator.populate();
    }

    @AfterEach
        //delete all data
    void teardown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        //delete everything
        em.createNativeQuery("TRUNCATE TABLE * RESTART IDENTITY CASCADE")
                .executeUpdate();
        em.getTransaction().commit();

        em.close();
    }

    @AfterAll
    static void tearDownOnce() {
        if (emf != null) emf.close();
    }

    @Test
    void read() {
        //get objects with the same id
        Object expected = TemplatePopulator.fetch().get(1);
        Object actual = dao.read(1L);
        assertEquals(expected, actual);
    }

    @Test
    void readAll() {
        List<Template> expected = TemplatePopulator.fetch();
        List<Template> actual = dao.readAll();
        assertThat( actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    void create() {
        Template expected = Template.builder().title("testField").build();
        expected.addField(Field.builder().fieldType(FieldType.CHECKBOX).title("testField").build());
        Template created = dao.create(expected);
        Template actualinDB = dao.read(created.getId());
        assertEquals(expected, actualinDB);
        assertEquals(created, actualinDB);
    }

    @Test
    void update() { //it shall not do anything
        long id = 1;
        Object Actual = dao.update(id, new Template());
        assertNull(Actual);
    }

    @Test
    void delete() { //sunny day test
        long id = 1;
        dao.delete(id);
        assertThrows(NoResultException.class, () -> dao.read(id));
    }
}