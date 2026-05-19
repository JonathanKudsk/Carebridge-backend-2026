package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.Handbook;
import com.carebridge.entities.HandbookTab;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandbookDAOTest {

    private static EntityManagerFactory emf;
    private HandbookDAO handbookDAO;

    @BeforeAll
    static void setupClass() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
    }

    @BeforeEach
    void setup() {
        handbookDAO = HandbookDAO.getInstance();
        clearDatabase();
    }

    // Clears handbook related tables before each test to ensure isolated test cases
    private void clearDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM HandbookTab").executeUpdate();
            em.createQuery("DELETE FROM Handbook").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    @DisplayName("Create handbook with tabs")
    void createShouldPersistHandbookWithTabs() {
        Handbook handbook = new Handbook("Institution Handbook");
        handbook.addHandbookTab(new HandbookTab("Rules", 1));
        handbook.addHandbookTab(new HandbookTab("Emergency", 2));

        Handbook createdHandbook = handbookDAO.create(handbook);

        assertNotNull(createdHandbook.getId());
        assertEquals(2, createdHandbook.getHandbookTabs().size());
    }

    @Test
    @DisplayName("Read handbook including tabs")
    void readShouldReturnHandbookWithTabs() {
        Handbook handbook = new Handbook("Institution Handbook");
        handbook.addHandbookTab(new HandbookTab("Rules", 1));

        Handbook createdHandbook = handbookDAO.create(handbook);
        Handbook foundHandbook = handbookDAO.read(createdHandbook.getId());

        assertNotNull(foundHandbook);
        assertEquals("Institution Handbook", foundHandbook.getTitle());
        assertFalse(foundHandbook.getHandbookTabs().isEmpty());
    }

    @Test
    @DisplayName("Read should throw exception when handbook does not exist")
    void readShouldThrowWhenHandbookDoesNotExist() {
        assertThrows(EntityNotFoundException.class, () -> handbookDAO.read(1L));
    }

    @Test
    @DisplayName("Read all should return all handbooks")
    void readAllShouldReturnAllHandbooks() {
        handbookDAO.create(new Handbook("Handbook One"));
        handbookDAO.create(new Handbook("Handbook Two"));

        List<Handbook> handbooks = handbookDAO.readAll();

        assertEquals(2, handbooks.size());
    }

    @Test
    @DisplayName("Update should update handbook title")
    void updateShouldUpdateHandbookTitle() {
        Handbook createdHandbook = handbookDAO.create(new Handbook("Old Title"));
        Handbook result = handbookDAO.update(createdHandbook.getId(), new Handbook("New Title"));

        assertEquals("New Title", result.getTitle());
    }
}