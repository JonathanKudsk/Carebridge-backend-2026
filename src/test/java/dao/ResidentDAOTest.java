package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResidentDAOTest {
    private EntityManagerFactory emf;
    private ResidentDAO residentDAO;
    private UserDAO userDAO;

    private Resident testResident;
    private User guardian;
    private Resident linkedResident;
    private Resident unlinkedResident;

    @BeforeAll
    void initOnce() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        residentDAO = ResidentDAO.getInstance();
        userDAO = UserDAO.getInstance();
    }

    @BeforeEach
    public void resetDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE resident_user, resident, users RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }

        testResident = residentDAO.create(new Resident("Bente", "Bentesen", "111111-1111", null, null));

        String suffix = UUID.randomUUID().toString();

        guardian = new User();
        guardian.setName("Guardian " + suffix);
        guardian.setEmail("guardian-" + suffix + "@example.com");
        guardian.setRole(Role.GUARDIAN);
        guardian.setPassword("test123");
        guardian = userDAO.create(guardian);

        linkedResident = new Resident();
        linkedResident.setFirstName("Anna-" + suffix);
        linkedResident.setLastName("Linked");
        linkedResident.setCprNr("222222-2222");
        linkedResident = residentDAO.create(linkedResident);

        unlinkedResident = new Resident();
        unlinkedResident.setFirstName("Bea-" + suffix);
        unlinkedResident.setLastName("Unlinked");
        unlinkedResident.setCprNr("333333-3333");
        unlinkedResident = residentDAO.create(unlinkedResident);

        guardian = userDAO.linkResidents(guardian.getId(), List.of(linkedResident));
    }

    @AfterEach
    public void cleanup() {
        try {
            if (guardian != null) {
                userDAO.clearResidents(guardian.getId());
            }
        } catch (Exception ignored) {
        }

        try {
            if (linkedResident != null) {
                residentDAO.delete(linkedResident.getId());
            }
        } catch (Exception ignored) {
        }

        try {
            if (unlinkedResident != null) {
                residentDAO.delete(unlinkedResident.getId());
            }
        } catch (Exception ignored) {
        }

        try {
            if (testResident != null) {
                residentDAO.delete(testResident.getId());
            }
        } catch (Exception ignored) {
        }

        try {
            if (guardian != null) {
                userDAO.delete(guardian.getId());
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testCreate() {
        Resident resident = new Resident("Jens", "Jensen", "444444-4444", null, null);

        Resident created = residentDAO.create(resident);

        assertNotNull(created.getId());
        assertEquals("Jens", created.getFirstName());
    }

    @Test
    public void testRead() {
        Resident found = residentDAO.read(testResident.getId());

        assertEquals(testResident.getId(), found.getId());
        assertEquals("Bente", found.getFirstName());
        assertEquals("Bentesen", found.getLastName());
        assertEquals("111111-1111", found.getCprNr());
    }

    @Test
    public void testDeactivate() {
        residentDAO.deactivate(testResident.getId());
        Resident deactivated = residentDAO.read(testResident.getId());

        assertFalse(deactivated.isActive());
    }

    @Test
    public void testGetAllSortedForGuardianOnlyReturnsLinkedResidents() {
        List<Resident> residents = residentDAO.getAllSortedForGuardian(guardian.getId());

        assertEquals(1, residents.size());
        assertEquals(linkedResident.getId(), residents.get(0).getId());
    }

    @Test
    public void testGetAllSortedForGuardianOrdersByFirstName() {
        Resident anotherLinkedResident = new Resident();
        anotherLinkedResident.setFirstName("Alica-" + UUID.randomUUID());
        anotherLinkedResident.setLastName("Linked");
        anotherLinkedResident.setCprNr("555555-5555");
        anotherLinkedResident = residentDAO.create(anotherLinkedResident);

        guardian = userDAO.linkResidents(guardian.getId(), List.of(linkedResident, anotherLinkedResident));

        List<Resident> residents = residentDAO.getAllSortedForGuardian(guardian.getId());

        assertEquals(2, residents.size());
        assertTrue(residents.get(0).getFirstName().compareTo(residents.get(1).getFirstName()) <= 0);

        try {
            userDAO.clearResidents(guardian.getId());
        } catch (Exception ignored) {
        }

        residentDAO.delete(anotherLinkedResident.getId());
    }
}
