package serviceTest;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ScheduleConflictException;
import com.carebridge.services.mappers.ShiftService;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShiftServiceTest {

    private static ShiftDAO shiftDAO;
    private static ShiftService shiftService;

    private static final Long USER_ID = 1L;

    private static final LocalDateTime EXISTING_START = LocalDateTime.of(2025, 1, 1, 8, 0);
    private static final LocalDateTime EXISTING_END   = LocalDateTime.of(2025, 1, 1, 16, 0);

    @BeforeAll
    static void setUp() {
        HibernateConfig.getEntityManagerFactoryForTest();
        shiftDAO     = ShiftDAO.getInstance();
        shiftService = ShiftService.getInstance();
    }

    @BeforeEach
    void insertExistingShift() {
        shiftDAO.create(buildShift(USER_ID, EXISTING_START, EXISTING_END));
    }

    @AfterEach
    void cleanUp() {
        shiftDAO.readAll().forEach(s -> shiftDAO.delete(s.getId()));
    }

    @Test
    @Order(1)
    void shouldDetectOverlapWhenShiftsShareMinute() {
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 15, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 17, 0);

        boolean overlapDetected;
        try {
            shiftService.validateNoOverlap(USER_ID, newStart, newEnd);
            overlapDetected = false;
        } catch (ScheduleConflictException e) {
            overlapDetected = true;
        }

        assertTrue(overlapDetected, "Forventede overlap når vagter deler tid");
    }

    @Test
    @Order(2)
    void shouldNotDetectOverlapWhenShiftsAreAdjacent() {
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 16, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 20, 0);

        assertDoesNotThrow(
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd),
                "Tilstødende vagter må ikke betragtes som overlap"
        );
    }

    @Test
    @Order(3)
    void shouldThrowScheduleConflictExceptionOnOverlap() {
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 14, 0);

        ScheduleConflictException exception = assertThrows(
                ScheduleConflictException.class,
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd)
        );

        assertEquals("Medarbejderen er allerede planlagt i dette tidsrum", exception.getMessage());
    }

    @Test
    @Order(4)
    void shouldAllowAssignmentWhenNoOverlap() {
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 17, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 21, 0);

        assertDoesNotThrow(
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd),
                "Vagt uden overlap må oprettes uden fejl"
        );
    }


    private Shift buildShift(Long userId, LocalDateTime start, LocalDateTime end) {
        Shift shift = new Shift();
        shift.setAssignedUserId(userId);
        shift.setStartShift(start);
        shift.setEndShift(end);
        shift.setShiftType("DAG");
        shift.setLocation("Afdeling A");
        shift.setStatus("PLANLAGT");
        shift.setPlanPeriodId(1L);
        shift.setCreatedBy(1L);
        return shift;
    }
}
