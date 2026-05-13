package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.MedicationChartDAO;
import com.carebridge.dao.impl.MedicationDAO;
import com.carebridge.entities.Medication;
import com.carebridge.entities.MedicationChart;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ConflictException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MedicationDAOTest {

    private EntityManagerFactory emf;
    private MedicationDAO medicationDAO;
    private MedicationChartDAO medicationChartDAO;
    private Long chartId;

    @BeforeAll
    void setupClass() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        medicationDAO = MedicationDAO.getInstance();
        medicationChartDAO = MedicationChartDAO.getInstance();
    }

    @BeforeEach
    void setupChart() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Resident resident = new Resident();
            resident.setFirstName("Test");
            resident.setLastName("Resident");
            resident.setCprNr("010101-" + System.nanoTime() % 10000);
            em.persist(resident);

            MedicationChart chart = new MedicationChart();
            chart.setResident(resident);
            em.persist(chart);

            em.getTransaction().commit();
            chartId = chart.getId();
        }
    }

    @AfterEach
    void cleanup() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("TRUNCATE TABLE medications, medication_charts, resident RESTART IDENTITY CASCADE").executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    void testVersionIsZeroAfterCreate() {
        Medication med = new Medication("Paracetamol", "500mg", "Twice daily",
                null, null, "Dr. Hansen", null, true);

        Medication created = medicationChartDAO.addMedication(chartId, med);

        assertNotNull(created.getVersion(), "Version should be set after persist");
        assertEquals(0L, created.getVersion(), "Version should start at 0");
    }

    @Test
    void testVersionIncrementsAfterUpdate() {
        Medication med = new Medication("Ibuprofen", "400mg", "Once daily",
                null, null, "Dr. Jensen", null, true);
        medicationChartDAO.addMedication(chartId, med);

        Medication fromDb = medicationDAO.read(med.getId());
        Long versionBefore = fromDb.getVersion();

        fromDb.setDosage("600mg");
        Medication updated = medicationDAO.update(fromDb.getId(), fromDb);

        assertEquals(versionBefore + 1, updated.getVersion(),
                "Version should increment by 1 after update");
    }

    @Test
    void testConflictWhenVersionMismatch() {
        Medication med = new Medication("Aspirin", "100mg", "Once daily",
                null, null, "Dr. Nielsen", null, true);
        medicationChartDAO.addMedication(chartId, med);

        // Doctor A reads the medication (version = 0)
        Medication doctorAView = medicationDAO.read(med.getId());
        Long doctorAVersion = doctorAView.getVersion();

        // Doctor B updates it first (version becomes 1)
        Medication doctorBView = medicationDAO.read(med.getId());
        doctorBView.setDosage("200mg");
        medicationDAO.update(doctorBView.getId(), doctorBView);

        // Doctor A tries to update with stale version — controller detects this
        Medication freshFromDb = medicationDAO.read(med.getId());
        boolean versionConflict = !doctorAVersion.equals(freshFromDb.getVersion());

        assertTrue(versionConflict,
                "Conflict should be detected: Doctor A's version is outdated after Doctor B updated");
    }

    @Test
    void testSuccessfulUpdateWithCorrectVersion() {
        Medication med = new Medication("Metformin", "850mg", "Twice daily",
                null, null, "Dr. Pedersen", null, true);
        medicationChartDAO.addMedication(chartId, med);

        Medication fromDb = medicationDAO.read(med.getId());
        Long expectedVersion = fromDb.getVersion();

        // Simulate controller: version matches, proceed with update
        assertDoesNotThrow(() -> {
            fromDb.setNotes("Take with food");
            medicationDAO.update(fromDb.getId(), fromDb);
        });

        Medication afterUpdate = medicationDAO.read(fromDb.getId());
        assertEquals(expectedVersion + 1, afterUpdate.getVersion());
        assertEquals("Take with food", afterUpdate.getNotes());
    }
}
