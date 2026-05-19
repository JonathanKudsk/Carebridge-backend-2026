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

/**
 * Integration tests for SavingsGoal endpoints.
 * NOTE: Requires US-4a (Budget) to be merged first.
 * The POST /budgets endpoint and Budget entity must exist before these tests can run fully.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SavingsGoalTest {

    private static String adminToken;
    private static int residentId;
    private static int budgetId;
    private static int savingsGoalId;
    private Javalin app;

    @BeforeAll
    public void setup() throws Exception {
        HibernateConfig.setTest(true);
        app = ApplicationConfig.startServer(7075);
        Populator.populate(HibernateConfig.getEntityManagerFactoryForTest());
        RestAssured.baseURI = "http://localhost:7075/api";

        adminToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin@carebridge.io\", \"password\":\"admin123\"}")
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
    public void testSetupResidentAndBudget() {
        residentId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"firstName\":\"SavingsTest\",\"lastName\":\"Resident\",\"cprNr\":\"030303-3030\"}")
                .when()
                .post("/residents/create")
                .then()
                .statusCode(201)
                .extract().path("id");

        Assertions.assertTrue(residentId > 0);

        // US-4a endpoint — creates a Budget linked to the resident
        budgetId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"residentId\":%d,\"income\":5000.0,\"fixedExpenses\":1000.0,\"variableExpenses\":500.0,\"pocketMoneyAmount\":200.0,\"savingsAmount\":300.0}",
                        residentId))
                .when()
                .post("/budgets")
                .then()
                .statusCode(201)
                .extract().path("id");

        Assertions.assertTrue(budgetId > 0);
    }

    @Test
    @Order(2)
    public void testCreateSavingsGoal() {
        String payload = String.format("""
                {
                    "budgetId": %d,
                    "goalName": "Vacation Fund",
                    "targetAmount": 5000.0,
                    "monthlySavingAmount": 200.0,
                    "currentBalance": 0.0
                }
                """, budgetId);

        savingsGoalId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/savings-goals")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("goalName", equalTo("Vacation Fund"))
                .body("targetAmount", equalTo(5000.0f))
                .body("monthlySavingAmount", equalTo(200.0f))
                .body("currentBalance", equalTo(0.0f))
                .body("progressPercentage", equalTo(0.0f))
                .body("budgetId", equalTo(budgetId))
                .extract().path("id");

        Assertions.assertTrue(savingsGoalId > 0);
    }

    @Test
    @Order(3)
    public void testReadSavingsGoal() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/savings-goals/" + savingsGoalId)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("id", equalTo(savingsGoalId))
                .body("goalName", equalTo("Vacation Fund"))
                .body("budgetId", equalTo(budgetId));
    }

    @Test
    @Order(4)
    public void testUpdateSavingsGoalBalance() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"currentBalance\": 1000.0}")
                .when()
                .put("/savings-goals/" + savingsGoalId)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("currentBalance", equalTo(1000.0f))
                .body("progressPercentage", equalTo(20.0f));
    }

    @Test
    @Order(5)
    public void testUpdateSavingsGoalNameAndMonthly() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"goalName\": \"New Car Fund\", \"monthlySavingAmount\": 300.0}")
                .when()
                .put("/savings-goals/" + savingsGoalId)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("goalName", equalTo("New Car Fund"))
                .body("monthlySavingAmount", equalTo(300.0f));
    }

    @Test
    @Order(6)
    public void testCreateSavingsGoalMissingGoalName() {
        String payload = String.format(
                "{\"budgetId\":%d,\"goalName\":\"\",\"targetAmount\":5000.0,\"monthlySavingAmount\":200.0,\"currentBalance\":0.0}",
                budgetId);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/savings-goals")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(7)
    public void testCreateSavingsGoalNegativeTarget() {
        String payload = String.format(
                "{\"budgetId\":%d,\"goalName\":\"Bad Goal\",\"targetAmount\":-100.0,\"monthlySavingAmount\":200.0,\"currentBalance\":0.0}",
                budgetId);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/savings-goals")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(8)
    public void testCreateMultipleSavingsGoalsAndGetByResident() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"budgetId\":%d,\"goalName\":\"Emergency Fund\",\"targetAmount\":10000.0,\"monthlySavingAmount\":500.0,\"currentBalance\":2000.0}",
                        budgetId))
                .when()
                .post("/savings-goals")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("goalName", equalTo("Emergency Fund"))
                .body("progressPercentage", equalTo(20.0f));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/savings-goals/resident/" + residentId)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("goalName", hasItem("Emergency Fund"));
    }
}
