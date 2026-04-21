package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
import com.carebridge.services.TotpService;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityTest {

    private Javalin app;
    private final TotpService totpService = new TotpService();

    // State shared between ordered tests — mirrors the real browser session flow
    private String adminSetupTempToken;
    private String adminTotpSecret;
    private String aliceVerifyTempToken;
    private String aliceFullToken;

    @BeforeAll
    void setup() {
        HibernateConfig.setTest(true);
        app = ApplicationConfig.startServer(7070);
        Populator.populate(HibernateConfig.getEntityManagerFactoryForTest());
        RestAssured.baseURI = "http://localhost:7070/api";
    }

    @AfterAll
    void teardown() {
        ApplicationConfig.stopServer(app);
    }

    // AC1 — Fresh user (no TOTP secret) gets requiresTotpSetup and a temp token; no full JWT
    @Test
    @Order(1)
    void freshUser_loginReturnsSetupRequired() {
        adminSetupTempToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin@carebridge.io\",\"password\":\"admin123\"}")
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("requiresTotpSetup", equalTo(true))
                .body("tempToken", notNullValue())
                .body("token", nullValue())
                .extract().path("tempToken");
    }

    // AC2 — SETUP-scoped temp token must be rejected on a protected endpoint
    @Test
    @Order(2)
    void setupScopedTempToken_cannotAccessProtectedEndpoint() {
        given()
                .header("Authorization", "Bearer " + adminSetupTempToken)
                .get("/events")
                .then()
                .log().ifValidationFails()
                .statusCode(401);
    }

    // GET /2fa/setup returns a secret and an otpauthUri for the QR code
    @Test
    @Order(3)
    void totpSetup_returnsSecretAndQrUri() {
        adminTotpSecret = given()
                .header("Authorization", "Bearer " + adminSetupTempToken)
                .get("/auth/2fa/setup")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("secret", notNullValue())
                .body("otpauthUri", notNullValue())
                .extract().path("secret");
    }

    // AC3 — Wrong code at /2fa/confirm is rejected with 401 and an error message
    @Test
    @Order(4)
    void totpConfirm_withWrongCode_returns401() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminSetupTempToken)
                .body("{\"code\":\"000000\"}")
                .post("/auth/2fa/confirm")
                .then()
                .log().ifValidationFails()
                .statusCode(401)
                .body("msg", notNullValue());
    }

    // Correct code at /2fa/confirm enables TOTP and returns a full JWT
    @Test
    @Order(5)
    void totpConfirm_withCorrectCode_returnsFullToken() throws Exception {
        String code = totpService.generateCurrentCode(adminTotpSecret);
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminSetupTempToken)
                .body("{\"code\":\"" + code + "\"}")
                .post("/auth/2fa/confirm")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("token", notNullValue());
    }

    // Bugfix: a user who has a secret saved but totp_enabled=false must get requiresTotpSetup,
    // not requires2FA — the abandoned setup flow must restart from scratch
    @Test
    @Order(6)
    void abandonedSetup_loginSendsToSetupNotVerify() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"partial@carebridge.io\",\"password\":\"password123\"}")
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("requiresTotpSetup", equalTo(true))
                .body("requires2FA", anyOf(nullValue(), equalTo(false)));
    }

    // AC4 — Returning user with totp_enabled=true gets requires2FA and a temp token; no full JWT
    @Test
    @Order(7)
    void returningUser_loginReturnsVerifyRequired() {
        aliceVerifyTempToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"alice@carebridge.io\",\"password\":\"password123\"}")
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("requires2FA", equalTo(true))
                .body("tempToken", notNullValue())
                .body("token", nullValue())
                .extract().path("tempToken");
    }

    // AC4 — Wrong code at /2fa/verify is rejected with 401
    @Test
    @Order(8)
    void totpVerify_withWrongCode_returns401() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + aliceVerifyTempToken)
                .body("{\"code\":\"000000\"}")
                .post("/auth/2fa/verify")
                .then()
                .log().ifValidationFails()
                .statusCode(401);
    }

    // AC4 — Correct code at /2fa/verify returns a full JWT
    @Test
    @Order(9)
    void totpVerify_withCorrectCode_returnsFullToken() throws Exception {
        String code = totpService.generateCurrentCode(Populator.ALICE_TOTP_SECRET);
        aliceFullToken = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + aliceVerifyTempToken)
                .body("{\"code\":\"" + code + "\"}")
                .post("/auth/2fa/verify")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().path("token");
    }

    // A full JWT returned by the 2FA flow must be accepted on protected endpoints
    @Test
    @Order(10)
    void fullToken_canAccessProtectedEndpoint() {
        given()
                .header("Authorization", "Bearer " + aliceFullToken)
                .get("/event-types")
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    // Login within the grace period skips the 2FA prompt and returns a full JWT directly
    @Test
    @Order(11)
    void loginWithinGracePeriod_returnsFullTokenDirectly() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"grace@carebridge.io\",\"password\":\"password123\"}")
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("token", notNullValue())
                .body("requiresTotpSetup", anyOf(nullValue(), equalTo(false)))
                .body("requires2FA", anyOf(nullValue(), equalTo(false)));
    }
}
