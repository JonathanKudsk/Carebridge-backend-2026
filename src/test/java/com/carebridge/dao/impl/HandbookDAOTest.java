package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.Handbook;
import com.carebridge.entities.HandbookTab;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandbookDAOTest
{

    private static EntityManagerFactory emf;

    private HandbookDAO handbookDAO;


    @BeforeAll
    static void setupClass()
    {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
    }


    @BeforeEach
    void setup()
    {
        handbookDAO = HandbookDAO.getInstance();

        clearDatabase();
    }


    /**
     * Clears handbook related tables before each test
     * to ensure isolated test cases.
     */
    private void clearDatabase()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            em.createQuery("DELETE FROM HandbookTab").executeUpdate();
            em.createQuery("DELETE FROM Handbook").executeUpdate();

            em.getTransaction().commit();
        }
    }


    @Test
    @DisplayName("Create handbook with tabs")
    void createShouldPersistHandbookWithTabs()
    {
        Handbook handbook =
                new Handbook("Institution Handbook");

        HandbookTab rulesTab =
                new HandbookTab(
                        "Rules",
                        "<p>Rules content</p>",
                        1,
                        Role.ADMIN
                );

        HandbookTab emergencyTab =
                new HandbookTab(
                        "Emergency",
                        "<p>Emergency content</p>",
                        2,
                        Role.CAREWORKER
                );

        handbook.addHandbookTab(rulesTab);
        handbook.addHandbookTab(emergencyTab);

        Handbook createdHandbook =
                handbookDAO.create(handbook);

        assertNotNull(createdHandbook.getId());

        assertEquals(
                2,
                createdHandbook.getHandbookTabs().size()
        );
    }


    @Test
    @DisplayName("Read handbook including tabs")
    void readShouldReturnHandbookWithTabs()
    {
        Handbook handbook =
                new Handbook("Institution Handbook");

        HandbookTab handbookTab =
                new HandbookTab(
                        "Rules",
                        "<p>Rules content</p>",
                        1,
                        Role.ADMIN
                );

        handbook.addHandbookTab(handbookTab);

        Handbook createdHandbook =
                handbookDAO.create(handbook);

        Handbook foundHandbook =
                handbookDAO.read(createdHandbook.getId());

        assertNotNull(foundHandbook);

        assertEquals(
                "Institution Handbook",
                foundHandbook.getTitle()
        );

        assertFalse(
                foundHandbook.getHandbookTabs().isEmpty()
        );
    }


    @Test
    @DisplayName("Read should throw exception when handbook does not exist")
    void readShouldThrowWhenHandbookDoesNotExist()
    {
        assertThrows(
                EntityNotFoundException.class,
                () -> handbookDAO.read(1L)
        );
    }


    @Test
    @DisplayName("Read all should return all handbooks")
    void readAllShouldReturnAllHandbooks()
    {
        Handbook handbookOne =
                new Handbook("Handbook One");

        Handbook handbookTwo =
                new Handbook("Handbook Two");

        handbookDAO.create(handbookOne);
        handbookDAO.create(handbookTwo);

        List<Handbook> handbooks =
                handbookDAO.readAll();

        assertEquals(2, handbooks.size());
    }


    @Test
    @DisplayName("Update should update handbook title")
    void updateShouldUpdateHandbookTitle()
    {
        Handbook handbook =
                new Handbook("Old Title");

        Handbook createdHandbook =
                handbookDAO.create(handbook);

        Handbook updatedHandbook =
                new Handbook("New Title");

        Handbook result =
                handbookDAO.update(
                        createdHandbook.getId(),
                        updatedHandbook
                );

        assertEquals(
                "New Title",
                result.getTitle()
        );
    }
}