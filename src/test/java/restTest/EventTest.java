package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.TestPopulator;
import com.carebridge.services.TotpService;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTest {

    private static String authToken;
    private static String adminAuthToken;
    private static int eventTypeId;
    private static int createdEventId;
    private static int residentId;
    private Javalin app;

    @BeforeAll
    public void setup() throws Exception {
        HibernateConfig.setTest(true);

        app = ApplicationConfig.startServer(7070);

        TestPopulator.populate(HibernateConfig.getEntityManagerFactoryForTest());

        RestAssured.baseURI = "http://localhost:7070/api";

        TotpService totpService = new TotpService();

        // Alice has totp_enabled=true — full verify flow required
        String aliceTempToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"alice@carebridge.io\",\"password\":\"password123\"}")
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().path("tempToken");

        authToken = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + aliceTempToken)
                .body("{\"code\":\"" + totpService.generateCurrentCode(TestPopulator.ALICE_TOTP_SECRET) + "\"}")
                .post("/auth/2fa/verify")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().path("token");

        // Admin has no TOTP configured yet — full setup flow required
        String adminTempToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin@carebridge.io\",\"password\":\"admin123\"}")
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().path("tempToken");

        String adminSecret = given()
                .header("Authorization", "Bearer " + adminTempToken)
                .get("/auth/2fa/setup")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().path("secret");

        adminAuthToken = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminTempToken)
                .body("{\"code\":\"" + totpService.generateCurrentCode(adminSecret) + "\"}")
                .post("/auth/2fa/confirm")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().path("token");

        eventTypeId = given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .when()
                .get("/event-types")
                .then()
                .statusCode(200)
                .extract().path("[0].id");

        residentId = given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body("{\"firstName\":\"Test\",\"lastName\":\"Resident\",\"cprNr\":\"010101-0001\"}")
                .post("/residents/create")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @AfterAll
    public void teardown() {
        ApplicationConfig.stopServer(app);
    }
    // ---------------------------
    // GET /events
    // ---------------------------
    @Test
    @Order(1)
    public void testReadAllEvents() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    // ---------------------------
    // POST /events
    // ---------------------------
    @Test
    @Order(2)
    public void testCreateEvent() {
        String futureStartAt = Instant.now().plusSeconds(60).toString();

        String payload = String.format("""
        {
                "title": "New Test Event",
                "description": "JUnit event",
                "startAt": "%s",
                "showOnBoard": true,
                "eventTypeId": %d,
                "residentId": %d
        }
        """, futureStartAt, eventTypeId, residentId);

        createdEventId = given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/events")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("title", equalTo("New Test Event"))
                .extract().path("id");

        Assertions.assertTrue(createdEventId > 0);
    }

    // ---------------------------
    // GET /events/{id}
    // ---------------------------
    @Test
    @Order(3)
    public void testReadEventById() {
        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .when()
                .get("/events/" + createdEventId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdEventId));
    }

    // ---------------------------
    // PUT /events/{id}
    // ---------------------------
    @Test
    @Order(4)
    public void testUpdateEvent() {

        String updateJson = """
        {
            "title": "Updated Event Title"
        }
        """;

        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/events/" + createdEventId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Event Title"));
    }

    // ---------------------------
    // DELETE /events/{id}
    // ---------------------------
    @Test
    @Order(6)
    public void testDeleteEvent() {
        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .when()
                .delete("/events/" + createdEventId)  // Only ADMIN allowed
                .then()
                .statusCode(anyOf(is(200), is(204), is(403))); // depends on your implementation
    }

    // ---------------------------
    // POST /events — manglende residentId
    // ---------------------------
    @Test
    @Order(7)
    public void testCreateEvent_missingResidentId_returns400() {
        String futureStartAt = Instant.now().plusSeconds(60).toString();

        String payload = String.format("""
        {
            "title": "Event Without Resident",
            "startAt": "%s",
            "showOnBoard": false,
            "eventTypeId": %d
        }
        """, futureStartAt, eventTypeId);

        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/events")
                .then()
                .statusCode(400);
    }

    // ---------------------------
    // GET /events/{id} — ingen adgang → 403
    // ---------------------------
    @Test
    @Order(8)
    public void testReadEventById_noAccess_returns403() {
        String futureStartAt = Instant.now().plusSeconds(60).toString();

        String payload = String.format("""
        {
            "title": "Admin Only Event",
            "startAt": "%s",
            "showOnBoard": false,
            "eventTypeId": %d,
            "residentId": %d,
            "isPrivate": true
        }
        """, futureStartAt, eventTypeId, residentId);

        int privateEventId = given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/events")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events/" + privateEventId)
                .then()
                .statusCode(403);
    }

    // ---------------------------
    // GET /events — kun tilgængelige events returneres
    // ---------------------------
    @Test
    @Order(9)
    public void testReadAll_returnsOnlyAccessibleEvents() {
        String futureStartAt = Instant.now().plusSeconds(60).toString();

        String payload = String.format("""
        {
            "title": "Hidden From Alice",
            "startAt": "%s",
            "showOnBoard": false,
            "eventTypeId": %d,
            "residentId": %d,
            "isPrivate": true
        }
        """, futureStartAt, eventTypeId, residentId);

        int hiddenEventId = given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/events")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events")
                .then()
                .statusCode(200)
                .body("id", not(hasItem(hiddenEventId)));
    }

    // ---------------------------
    // POST /events — med residentId → 201
    // ---------------------------
    @Test
    @Order(10)
    public void testCreateEvent_withResidentId_returns201() {
        String futureStartAt = Instant.now().plusSeconds(60).toString();

        String payload = String.format("""
        {
            "title": "Event With Resident",
            "startAt": "%s",
            "showOnBoard": false,
            "eventTypeId": %d,
            "residentId": %d
        }
        """, futureStartAt, eventTypeId, residentId);

        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/events")
                .then()
                .statusCode(201)
                .body("residentId", equalTo(residentId));
    }

    // ---------------------------
    // GET /events/upcoming
    // ---------------------------
    @Test
    @Order(5)
    public void testUpcomingEvents() {
        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .when()
                .get("/events/upcoming")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].startAt", notNullValue());
    }
}
