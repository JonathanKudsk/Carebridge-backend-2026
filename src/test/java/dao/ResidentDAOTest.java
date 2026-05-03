package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.entities.Resident;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResidentDAOTest {
    private EntityManagerFactory emf;
    private ResidentDAO residentDAO;
    private Resident testResident;


    @BeforeAll
    void initOnce () {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        residentDAO = ResidentDAO.getInstance();
    }

    @BeforeEach
    public void resetDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE Resident RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }

        testResident = residentDAO.create(new Resident("Bente", "Bentesen", "111111-1111", null, null));
    }

    @Test
    public void testCreate() {
        // Arrange
        Resident resident = new Resident("Jens", "Jensen", "222222-2222", null, null);

        // Act
        Resident created = residentDAO.create(resident);

        // Assert
        assertNotNull(created.getId());
        assertEquals("Jens", created.getFirstName());
        assertEquals(2, created.getId());
    }

    @Test
    public void testRead() {
        // Arrange in @BeforeEach

        // Act
        Resident found = residentDAO.read(testResident.getId());

        // Assert
        assertEquals(testResident.getId(), found.getId());
        assertEquals("Bente", found.getFirstName());
        assertEquals("Bentesen", found.getLastName());
        assertEquals("111111-1111", found.getCprNr());
    }

    @Test
    public void testDeactivate() {
        // Arrange in @BeforeEach

        // Act
        residentDAO.deactivate(testResident.getId());
        Resident deactivated = residentDAO.read(testResident.getId());

        // Assert
        assertFalse(deactivated.isActive());
    }
}
