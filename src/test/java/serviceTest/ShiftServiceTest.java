package serviceTest;

import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ScheduleConflictException;
import com.carebridge.services.mappers.ShiftService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShiftServiceTest {

    private ShiftDAO shiftDAO;
    private ShiftService shiftService;

    private static final Long USER_ID   = 1L;
    private static final Long SHIFT_ID  = 99L;

    private static final LocalDateTime EXISTING_START = LocalDateTime.of(2025, 1, 1, 8,  0);
    private static final LocalDateTime EXISTING_END   = LocalDateTime.of(2025, 1, 1, 16, 0);

    @BeforeEach
    void setUp() {
        shiftDAO     = Mockito.mock(ShiftDAO.class);
        shiftService = new ShiftService(shiftDAO);
    }

    @Test
    @Order(1)
    void shouldDetectOverlapWhenShiftsShareTime() {
        when(shiftDAO.findByAssignedUserId(USER_ID))
                .thenReturn(List.of(buildShift(SHIFT_ID, EXISTING_START, EXISTING_END)));

        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 15, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 17, 0);

        assertThrows(ScheduleConflictException.class,
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd),
                "Forventede overlap når vagter deler tid");
    }

    @Test
    @Order(2)
    void shouldNotDetectOverlapWhenShiftsAreAdjacent() {
        when(shiftDAO.findByAssignedUserId(USER_ID))
                .thenReturn(List.of(buildShift(SHIFT_ID, EXISTING_START, EXISTING_END)));

        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 16, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 20, 0);

        assertDoesNotThrow(
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd),
                "Tilstødende vagter må ikke betragtes som overlap");
    }

    @Test
    @Order(3)
    void shouldThrowWithCorrectMessageOnOverlap() {
        when(shiftDAO.findByAssignedUserId(USER_ID))
                .thenReturn(List.of(buildShift(SHIFT_ID, EXISTING_START, EXISTING_END)));

        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 14, 0);

        ScheduleConflictException ex = assertThrows(ScheduleConflictException.class,
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd));

        assertEquals("Medarbejderen er allerede planlagt i dette tidsrum", ex.getMessage());
    }

    @Test
    @Order(4)
    void shouldAllowShiftWhenNoOverlap() {
        when(shiftDAO.findByAssignedUserId(USER_ID))
                .thenReturn(List.of(buildShift(SHIFT_ID, EXISTING_START, EXISTING_END)));

        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 17, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 21, 0);

        assertDoesNotThrow(
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd),
                "Vagt uden overlap må oprettes uden fejl");
    }

    @Test
    @Order(5)
    void shouldAllowShiftWhenNoExistingShifts() {
        when(shiftDAO.findByAssignedUserId(USER_ID)).thenReturn(List.of());

        assertDoesNotThrow(
                () -> shiftService.validateNoOverlap(USER_ID, EXISTING_START, EXISTING_END),
                "Ingen eksisterende vagter bør ikke kaste exception");
    }

    @Test
    @Order(6)
    void shouldAllowUpdateWhenOnlyOverlapIsItself() {
        when(shiftDAO.findByAssignedUserId(USER_ID))
                .thenReturn(List.of(buildShift(SHIFT_ID, EXISTING_START, EXISTING_END)));

        assertDoesNotThrow(
                () -> shiftService.validateNoOverlapOnUpdate(SHIFT_ID, USER_ID, EXISTING_START, EXISTING_END),
                "En vagt må godt overlappe med sig selv ved opdatering");
    }

    @Test
    @Order(7)
    void shouldDetectOverlapOnUpdateWithOtherShift() {
        Long otherShiftId = 100L;
        when(shiftDAO.findByAssignedUserId(USER_ID))
                .thenReturn(List.of(buildShift(otherShiftId, EXISTING_START, EXISTING_END)));

        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 18, 0);

        assertThrows(ScheduleConflictException.class,
                () -> shiftService.validateNoOverlapOnUpdate(SHIFT_ID, USER_ID, newStart, newEnd),
                "Overlap med anden vagt skal kastes som exception");
    }

    @Test
    @Order(8)
    void shouldAllowUpdateWhenNewTimesHaveNoOverlap() {
        Long otherShiftId = 100L;
        when(shiftDAO.findByAssignedUserId(USER_ID))
                .thenReturn(List.of(buildShift(otherShiftId, EXISTING_START, EXISTING_END)));

        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 17, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 21, 0);

        assertDoesNotThrow(
                () -> shiftService.validateNoOverlapOnUpdate(SHIFT_ID, USER_ID, newStart, newEnd),
                "Opdatering uden overlap må ikke kaste exception");
    }

    private Shift buildShift(Long id, LocalDateTime start, LocalDateTime end) {
        Shift shift = new Shift();
        shift.setId(id);
        shift.setAssignedUserId(USER_ID);
        shift.setStartShift(start);
        shift.setEndShift(end);
        return shift;
    }
}
