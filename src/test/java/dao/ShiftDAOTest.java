package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShiftDAOTest {

    private ShiftDAO shiftDAO;
    private Shift testShift;

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        HibernateConfig.getEntityManagerFactoryForTest();
        shiftDAO = ShiftDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
        testShift = new Shift();
        testShift.setStartShift(LocalDateTime.now().plusHours(1));
        testShift.setEndShift(LocalDateTime.now().plusHours(2));
        testShift.setShiftType("DAY");
        testShift.setLocation("Unit 1");
        testShift.setStatus("ACTIVE");
        testShift.setPlanPeriodId(1L);
        testShift.setAssignedUserId(1L);
        testShift.setCreatedBy(1L);
        shiftDAO.create(testShift);
    }

    @AfterEach
    public void cleanup() {
        if (testShift != null) {
            try {
                shiftDAO.delete(testShift.getId());
            } catch (ApiRuntimeException e) {
                if (e.getStatusCode() != 404) {
                    throw e;
                }
            }
        }
    }

    @Test
    public void testCreateShift() {
        Shift shift = new Shift();
        shift.setStartShift(LocalDateTime.now().plusHours(3));
        shift.setEndShift(LocalDateTime.now().plusHours(4));
        shift.setShiftType("NIGHT");
        shift.setLocation("Unit 2");
        shift.setStatus("ACTIVE");
        shift.setPlanPeriodId(2L);
        shift.setAssignedUserId(2L);
        shift.setCreatedBy(1L);

        Shift created = shiftDAO.create(shift);
        assertNotNull(created.getId());

        shiftDAO.delete(created.getId());
    }

    @Test
    public void testReadShift() {
        Shift read = shiftDAO.read(testShift.getId());
        assertNotNull(read);
        assertEquals("Unit 1", read.getLocation());
    }

    @Test
    public void testUpdateShift() {
        testShift.setLocation("Updated Unit");
        testShift.setStatus("UPDATED");

        Shift updated = shiftDAO.update(testShift.getId(), testShift);
        assertEquals("Updated Unit", updated.getLocation());
        assertEquals("UPDATED", updated.getStatus());
    }

    @Test
    public void testDeleteShift() {
        Shift shift = new Shift();
        shift.setStartShift(LocalDateTime.now().plusHours(5));
        shift.setEndShift(LocalDateTime.now().plusHours(6));
        shift.setShiftType("EVENING");
        shift.setLocation("Unit 3");
        shift.setStatus("ACTIVE");
        shift.setPlanPeriodId(3L);
        shift.setAssignedUserId(3L);
        shift.setCreatedBy(1L);

        Shift created = shiftDAO.create(shift);
        shiftDAO.delete(created.getId());
        assertNull(shiftDAO.read(created.getId()));
    }

    @Test
    public void testReadAllShifts() {
        Shift shift2 = new Shift();
        shift2.setStartShift(LocalDateTime.now().plusHours(7));
        shift2.setEndShift(LocalDateTime.now().plusHours(8));
        shift2.setShiftType("DAY");
        shift2.setLocation("Unit 4");
        shift2.setStatus("ACTIVE");
        shift2.setPlanPeriodId(4L);
        shift2.setAssignedUserId(4L);
        shift2.setCreatedBy(1L);

        Shift created = shiftDAO.create(shift2);
        List<Shift> shifts = shiftDAO.readAll();
        assertTrue(shifts.stream().anyMatch(s -> s.getId().equals(testShift.getId())));
        assertTrue(shifts.stream().anyMatch(s -> s.getId().equals(created.getId())));

        shiftDAO.delete(created.getId());
    }

    @Test
    public void testCreateShiftInvalidTimes() {
        Shift invalid = new Shift();
        invalid.setStartShift(LocalDateTime.now().plusHours(2));
        invalid.setEndShift(LocalDateTime.now().plusHours(1));
        invalid.setShiftType("DAY");
        invalid.setLocation("Unit 5");
        invalid.setStatus("ACTIVE");
        invalid.setPlanPeriodId(5L);
        invalid.setAssignedUserId(5L);
        invalid.setCreatedBy(1L);

        assertThrows(ApiRuntimeException.class, () -> shiftDAO.create(invalid));
    }
}

