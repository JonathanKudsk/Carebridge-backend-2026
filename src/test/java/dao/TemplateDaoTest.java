package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.TemplateDAO;
import com.carebridge.entities.Field;
import com.carebridge.entities.Template;
import com.carebridge.enums.FieldType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import populator.TemplatePopulator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


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
        em.createNativeQuery("TRUNCATE TABLE templates, fields RESTART IDENTITY CASCADE")
                .executeUpdate();
        em.getTransaction().commit();

        em.close();
    }

    @Test
    void read() {
        Template expected = TemplatePopulator.fetch().get(0);
        Template actual = dao.read(1L);
        assertTemplateMatches(expected, actual);
    }

    @Test
    void readAll() {
        List<Template> expected = TemplatePopulator.fetch();
        List<Template> actual = dao.readAll();
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertTemplateSummaryMatches(expected.get(i), actual.get(i));
        }
    }

    @Test
    void create() {
        Template expected = Template.builder().title("testField").build();
        expected.addField(Field.builder().fieldType(FieldType.CHECKBOX).title("testField").build());
        Template created = dao.create(expected);
        Template actualinDB = dao.read(created.getId());
        assertTemplateMatches(expected, actualinDB);
        assertTemplateMatches(created, actualinDB);
    }

    @Test
    void update() { //it shall not do anything
        long id = 1;
        Template Actual = dao.update(id, new Template());
        assertNull(Actual);
    }

    @Test
    void delete() { //sunny day test
        long id = 1;
        dao.delete(id);
        assertNull(dao.read(id));
    }

    private void assertTemplateMatches(Template expected, Template actual) {
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getFields().size(), actual.getFields().size());

        for (int i = 0; i < expected.getFields().size(); i++) {
            Field expectedField = expected.getFields().get(i);
            Field actualField = actual.getFields().get(i);

            assertEquals(expectedField.getTitle(), actualField.getTitle());
            assertEquals(expectedField.getFieldType(), actualField.getFieldType());
        }
    }

    private void assertTemplateSummaryMatches(Template expected, Template actual) {
        assertEquals(expected.getTitle(), actual.getTitle());
    }
}
