package serviceTest;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Resident;
import com.carebridge.services.ResidentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResidentServiceTest {

    private EntityManagerFactory emf;
    private ResidentDAO residentDAO;
    private ResidentService residentService;

    @BeforeAll
    void initOnce() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        residentDAO = ResidentDAO.getInstance();
        residentService = new ResidentService();
    }

    @BeforeEach
    void resetDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE resident_user, guardian_residents, resident RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    void getAllSortedReturnsMappedResidentResponseDTOs() {
        Resident bent = residentDAO.create(buildResident("Bent", "Berg", "020202-5678"));
        Resident anna = residentDAO.create(buildResident("Anna", "Andersen", "010101-1234"));

        List<ResidentResponseDTO> residents = residentService.getAllSorted(null);

        assertEquals(2, residents.size());
        assertNotNull(residents.get(0));
        assertEquals(anna.getId(), residents.get(0).getId());
        assertEquals("Anna", residents.get(0).getFirstName());
        assertEquals("Andersen", residents.get(0).getLastName());
        assertEquals("010101-1234", residents.get(0).getCprNr());

        assertNotNull(residents.get(1));
        assertEquals(bent.getId(), residents.get(1).getId());
        assertEquals("Bent", residents.get(1).getFirstName());
        assertEquals("Berg", residents.get(1).getLastName());
        assertEquals("020202-5678", residents.get(1).getCprNr());
    }

    private Resident buildResident(String firstName, String lastName, String cprNr) {
        Resident resident = new Resident();
        resident.setFirstName(firstName);
        resident.setLastName(lastName);
        resident.setCprNr(cprNr);
        return resident;
    }
}
