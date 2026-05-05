package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResidentDAOTest {

    private ResidentDAO residentDAO;
    private UserDAO userDAO;

    private User guardian;
    private Resident linkedResident;
    private Resident unlinkedResident;

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        EntityManagerFactory emfTest = HibernateConfig.getEntityManagerFactoryForTest();

        residentDAO = ResidentDAO.getInstance();
        userDAO = UserDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
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
        linkedResident.setCprNr("111111-1111");
        linkedResident = residentDAO.create(linkedResident);

        unlinkedResident = new Resident();
        unlinkedResident.setFirstName("Bea-" + suffix);
        unlinkedResident.setLastName("Unlinked");
        unlinkedResident.setCprNr("222222-2222");
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
            if (guardian != null) {
                userDAO.delete(guardian.getId());
            }
        } catch (Exception ignored) {
        }
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
        anotherLinkedResident.setCprNr("333333-3333");
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
