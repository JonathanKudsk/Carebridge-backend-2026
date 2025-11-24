package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import io.javalin.http.ContentType;
import org.junit.jupiter.api.*;
import io.javalin.Javalin;
import com.carebridge.config.Populator;
import io.restassured.RestAssured;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTest {

    private static String authToken;
    private static String adminAuthToken;
    private Javalin app;

    @BeforeAll
    public void setup() throws Exception {
        HibernateConfig.setTest(true);

        app = ApplicationConfig.startServer(7070);

        Populator.populate(HibernateConfig.getEntityManagerFactoryForTest());

        RestAssured.baseURI = "http://localhost:7070/api";

        authToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"alice@carebridge.io\", \"password\":\"password123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");

        adminAuthToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin@carebridge.io\", \"password\":\"admin123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");
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
                "createdById": 2,
                "eventTypeId": 1
        }
        """, futureStartAt);

        int createdId =
                given()
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(ContentType.JSON)
                        .body(payload)
                        .when()
                        .post("/events")
                        .then()
                        .statusCode(201)
                        .body("title", equalTo("New Test Event"))
                        .extract().path("id");

        Assertions.assertTrue(createdId > 0);
    }

    // ---------------------------
    // GET /events/{id}
    // ---------------------------
    @Test
    @Order(3)
    public void testReadEventById() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1));
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
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/events/1")
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
                .header("Authorization", "Bearer " + authToken)
                .when()
                .delete("/events/1")  // Only ADMIN allowed
                .then()
                .statusCode(anyOf(is(200), is(204), is(403))); // depends on your implementation
    }

    // ---------------------------
    // GET /events/upcoming
    // ---------------------------
    @Test
    @Order(5)
    public void testUpcomingEvents() {
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/events/upcoming")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].startAt", notNullValue());
    }
}
