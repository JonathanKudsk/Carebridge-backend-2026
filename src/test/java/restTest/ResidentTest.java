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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentTest {

    private static String adminAuthToken;
    private static String userAuthToken;
    private static int createdResidentId;
    private Javalin app;

    @BeforeAll
    public void setup() throws Exception {
        HibernateConfig.setTest(true);
        app = ApplicationConfig.startServer(7070);
        Populator.populate(HibernateConfig.getEntityManagerFactoryForTest());
        RestAssured.baseURI = "http://localhost:7070/api";


        adminAuthToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin@carebridge.io\", \"password\":\"admin123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");


        userAuthToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"alice@carebridge.io\", \"password\":\"password123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    @AfterAll
    public void teardown() {
        ApplicationConfig.stopServer(app);
    }

    @Test
    @Order(1)
    public void testCreateResident() {
        String payload = """
        {
                "firstName": "John",
                "lastName": "Doe",
                "cprNr": "101010-1010"
        }
        """;

        createdResidentId = given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/residents/create")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .extract().path("id");

        Assertions.assertTrue(createdResidentId > 0);
    }

    @Test
    @Order(2)
    public void testReadAllResidents() {
        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .when()
                .get("/residents")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("firstName", hasItem("John"));
    }

    @Test
    @Order(3)
    public void testReadResidentById() {
        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .when()
                .get("/residents/" + createdResidentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdResidentId))
                .body("firstName", equalTo("John"));
    }


    @Test
    @Order(4)
    public void testUpdateResident() {
        String updatePayload = """
        {
            "firstName": "Johnny",
            "lastName": "Updated"
        }
        """;

        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/residents/" + createdResidentId)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Johnny"))
                .body("lastName", equalTo("Updated"));
    }


    @Test
    @Order(5)
    public void testDeleteResident() {
        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .when()
                .delete("/residents/" + createdResidentId)
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + adminAuthToken)
                .when()
                .get("/residents/" + createdResidentId)
                .then()
                .statusCode(500);
    }


    @Test
    @Order(6)
    public void testCreateResidentAsUserFails() {
        String payload = "{\"firstName\": \"Fail\", \"lastName\": \"Test\"}";

        given()
                .header("Authorization", "Bearer " + userAuthToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/residents/create")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(7)
    public void testReadResidentByIdAsUserFails() {
        given()
                .header("Authorization", "Bearer " + userAuthToken)
                .when()
                .get("/residents/" + createdResidentId)
                .then()
                .statusCode(401);
    }
}