package serviceTest;

import com.carebridge.dao.impl.ShiftDAO;
import com.carebridge.entities.Shift;
import com.carebridge.exceptions.ScheduleConflictException;
import com.carebridge.services.mappers.ShiftService;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShiftServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("carebridge_test")
            .withUsername("test")
            .withPassword("test");

    private static ShiftDAO shiftDAO;
    private static ShiftService shiftService;

    private static final Long USER_ID = 1L;

    // Eksisterende vagt: 08:00 - 16:00
    private static final LocalDateTime EXISTING_START = LocalDateTime.of(2025, 1, 1, 8, 0);
    private static final LocalDateTime EXISTING_END   = LocalDateTime.of(2025, 1, 1, 16, 0);

    @BeforeAll
    static void setUp() {
        // Sæt system properties så HibernateConfig bruger test-containeren
        System.setProperty("DB_URL",      postgres.getJdbcUrl());
        System.setProperty("DB_USERNAME", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());

        shiftDAO     = ShiftDAO.getInstance();
        shiftService = ShiftService.getInstance();
    }

    @BeforeEach
    void insertExistingShift() {
        // Arrange — indsæt en kendt vagt i databasen før hver test
        Shift existing = buildShift(USER_ID, EXISTING_START, EXISTING_END);
        shiftDAO.create(existing);
    }

    @AfterEach
    void cleanUp() {
        // Ryd databasen mellem tests så de ikke påvirker hinanden
        shiftDAO.readAll().forEach(s -> shiftDAO.delete(s.getId()));
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void shouldDetectOverlapWhenShiftsShareMinute() {
        // Arrange — ny vagt 15:00-17:00 overlapper med eksisterende 08:00-16:00
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 15, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 17, 0);

        // Act
        boolean overlapDetected;
        try {
            shiftService.validateNoOverlap(USER_ID, newStart, newEnd);
            overlapDetected = false;
        } catch (ScheduleConflictException e) {
            overlapDetected = true;
        }

        // Assert
        assertTrue(overlapDetected, "Forventede overlap når vagter deler tid");
    }

    @Test
    @Order(2)
    void shouldNotDetectOverlapWhenShiftsAreAdjacent() {
        // Arrange — ny vagt starter præcis når den eksisterende slutter (16:00-20:00)
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 16, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 20, 0);

        // Act & Assert — ingen exception forventet ved tilstødende vagter
        assertDoesNotThrow(
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd),
                "Tilstødende vagter må ikke betragtes som overlap"
        );
    }

    @Test
    @Order(3)
    void shouldThrowScheduleConflictExceptionOnOverlap() {
        // Arrange — ny vagt 10:00-14:00 ligger inde i eksisterende 08:00-16:00
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 14, 0);

        // Act
        ScheduleConflictException exception = assertThrows(
                ScheduleConflictException.class,
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd)
        );

        // Assert
        assertEquals("Medarbejderen er allerede planlagt i dette tidsrum", exception.getMessage());
    }

    @Test
    @Order(4)
    void shouldAllowAssignmentWhenNoOverlap() {
        // Arrange — ny vagt 17:00-21:00 ligger helt efter eksisterende 08:00-16:00
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 17, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2025, 1, 1, 21, 0);

        // Act & Assert — ingen exception forventet
        assertDoesNotThrow(
                () -> shiftService.validateNoOverlap(USER_ID, newStart, newEnd),
                "Vagt uden overlap må oprettes uden fejl"
        );
    }

    // ── Hjælpemetode ──────────────────────────────────────────────────────────

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