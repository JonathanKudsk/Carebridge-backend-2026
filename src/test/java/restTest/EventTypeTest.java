package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    public class EventTypeTest {

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

    private static int createdEventTypeId;

    // ---------------------------
    // GET /eventtypes
    // ---------------------------
    @Test
    @Order(10)
    public void testReadAllEventTypes() {
        given()
                .header("Authorization", "Bearer " + authToken) // ANYONE role allows USER token
                .when()
                .get("/event-types")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    // ---------------------------
    // POST /eventtypes (ADMIN required)
    // ---------------------------
    @Test
    @Order(11)
    public void testCreateEventType() {

        String payload = """
    {
        "name": "New Test Event Type",
        "colorHex": "#1A2B3C"
    }
    """;

        createdEventTypeId =
                given()
                        .header("Authorization", "Bearer " + adminAuthToken) // Must use Admin token
                        .contentType(ContentType.JSON)
                        .body(payload)
                        .when()
                        .post("/event-types")
                        .then()
                        .statusCode(201)
                        .body("name", equalTo("New Test Event Type"))
                        .extract().path("id");

        Assertions.assertTrue(createdEventTypeId > 0);
    }

    // ---------------------------
    // GET /eventtypes/{id}
    // ---------------------------
    @Test
    @Order(12)
    public void testReadEventTypeById() {
        // Read the newly created ID to ensure it exists
        given()
                .header("Authorization", "Bearer " + authToken) // ANYONE role allows USER token
                .when()
                .get("/event-types/" + createdEventTypeId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdEventTypeId));
    }

    // ---------------------------
    // PUT /eventtypes/{id} (ADMIN required)
    // ---------------------------
    @Test
    @Order(13)
    public void testUpdateEventType() {

        String updateJson = """
    {
        "name": "Urgent Updated Type"
    }
    """;

        given()
                .header("Authorization", "Bearer " + adminAuthToken) // Must use Admin token
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/event-types/" + createdEventTypeId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Urgent Updated Type"));
    }

    // ---------------------------
    // DELETE /eventtypes/{id} (ADMIN required)
    // ---------------------------
    @Test
    @Order(14)
    public void testDeleteEventType() {
        given()
                .header("Authorization", "Bearer " + adminAuthToken) // Must use Admin token
                .when()
                .delete("/event-types/" + createdEventTypeId)
                .then()
                .statusCode(anyOf(is(200), is(204))); // 200/204 are common for successful delete
    }
}
