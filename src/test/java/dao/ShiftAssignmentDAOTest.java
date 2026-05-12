package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ShiftAssignmentDAO;
import com.carebridge.entities.ShiftAssignment;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShiftAssignmentDAOTest {

    private ShiftAssignmentDAO shiftAssignmentDAO;
    private ShiftAssignment testShiftAssignment;
    private final List<Long> createdIds = new ArrayList<>();

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        HibernateConfig.getEntityManagerFactoryForTest();
        shiftAssignmentDAO = ShiftAssignmentDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
        testShiftAssignment = new ShiftAssignment();
        testShiftAssignment.setShiftId(1L);
        testShiftAssignment.setUserId(1L);
        testShiftAssignment.setAssignedBy(1L);
        testShiftAssignment.setAssignedAt(Instant.now());
        shiftAssignmentDAO.create(testShiftAssignment);
        createdIds.add(testShiftAssignment.getId());
    }

    @AfterEach
    public void cleanup() {
        for (Long id : createdIds) {
            try {
                shiftAssignmentDAO.delete(id);
            } catch (Exception ignored) {}
        }
        createdIds.clear();
    }

    @Test
    public void testCreateShiftAssignment() {
        ShiftAssignment shiftAssignment = new ShiftAssignment();
        shiftAssignment.setShiftId(2L);
        shiftAssignment.setUserId(2L);
        shiftAssignment.setAssignedBy(1L);
        shiftAssignment.setAssignedAt(Instant.now());

        ShiftAssignment created = shiftAssignmentDAO.create(shiftAssignment);
        assertNotNull(created.getId());
        assertEquals(2L, created.getShiftId());
        assertEquals(2L, created.getUserId());

        shiftAssignmentDAO.delete(created.getId());
    }

    @Test
    public void testReadShiftAssignment() {
        ShiftAssignment read = shiftAssignmentDAO.read(testShiftAssignment.getId());
        assertNotNull(read);
        assertEquals(testShiftAssignment.getShiftId(), read.getShiftId());
        assertEquals(testShiftAssignment.getUserId(), read.getUserId());
    }

    @Test
    public void testUpdateShiftAssignment() {
        testShiftAssignment.setUserId(5L);
        ShiftAssignment updated = shiftAssignmentDAO.update(testShiftAssignment.getId(), testShiftAssignment);
        assertNotNull(updated);
        assertEquals(5L, updated.getUserId());
    }

    @Test
    public void testReadByUser() {
        List<ShiftAssignment> byUser = shiftAssignmentDAO.readByUser(testShiftAssignment.getUserId());
        assertTrue(byUser.stream().anyMatch(sa -> sa.getId().equals(testShiftAssignment.getId())));
    }

    @Test
    public void testReadByShift() {
        List<ShiftAssignment> byShift = shiftAssignmentDAO.readByShift(testShiftAssignment.getShiftId());
        assertTrue(byShift.stream().anyMatch(sa -> sa.getId().equals(testShiftAssignment.getId())));
    }

    @Test
    public void testReadAllShiftAssignments() {
        List<ShiftAssignment> all = shiftAssignmentDAO.readAll();
        assertFalse(all.isEmpty());
    }

    @Test
    public void testDeleteShiftAssignment() {
        ShiftAssignment shiftAssignment = new ShiftAssignment();
        shiftAssignment.setShiftId(3L);
        shiftAssignment.setUserId(3L);
        shiftAssignment.setAssignedBy(1L);
        shiftAssignment.setAssignedAt(Instant.now());

        ShiftAssignment created = shiftAssignmentDAO.create(shiftAssignment);
        shiftAssignmentDAO.delete(created.getId());
        assertNull(shiftAssignmentDAO.read(created.getId()));
    }
}

