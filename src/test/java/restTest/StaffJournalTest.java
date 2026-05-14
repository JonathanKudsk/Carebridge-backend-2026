package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.TestPopulator;
import com.carebridge.services.TotpService;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StaffJournalTest {

    private Javalin app;
    private final TotpService totpService = new TotpService();

    private String adminToken;
    private int crudJournalId;
    private int createdEntryId;
    private int createdContractId;
    private File pdfFile;
    private File txtFile;

    @BeforeAll
    void setup() throws Exception {
        HibernateConfig.setTest(true);
        app = ApplicationConfig.startServer(7070);
        TestPopulator.populate(HibernateConfig.getEntityManagerFactoryForTest());
        RestAssured.baseURI = "http://localhost:7070/api";

        // Get admin token via full TOTP setup flow (admin has no TOTP configured yet)
        String tempToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin@carebridge.io\",\"password\":\"admin123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("tempToken");

        String secret = given()
                .header("Authorization", "Bearer " + tempToken)
                .get("/auth/2fa/setup")
                .then()
                .statusCode(200)
                .extract().path("secret");

        adminToken = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + tempToken)
                .body("{\"code\":\"" + totpService.generateCurrentCode(secret) + "\"}")
                .post("/auth/2fa/confirm")
                .then()
                .statusCode(200)
                .extract().path("token");

        // Register a dedicated careworker for CRUD tests — journal is auto-created
        String crudEmail = "crud_careworker_" + System.currentTimeMillis() + "@test.com";
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "name": "CRUD Careworker",
                        "email": "%s",
                        "password": "pass123",
                        "displayName": "CRUD Worker",
                        "displayEmail": "%s",
                        "displayPhone": "111-1111-1111",
                        "internalEmail": "crud.internal@test.com",
                        "internalPhone": "222-2222-2222",
                        "role": "CAREWORKER"
                    }
                """, crudEmail, crudEmail))
                .post("/auth/register")
                .then()
                .statusCode(201);

        int crudUserId = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/users/by-email/" + crudEmail)
                .then()
                .statusCode(200)
                .extract().path("id");

        crudJournalId = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/staff-journals/user/" + crudUserId)
                .then()
                .statusCode(200)
                .extract().path("id");

        // Create minimal PDF temp file
        pdfFile = File.createTempFile("test-contract", ".pdf");
        byte[] pdfContent = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n%%EOF"
                .getBytes(StandardCharsets.ISO_8859_1);
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            fos.write(pdfContent);
        }
        pdfFile.deleteOnExit();

        // Create a non-PDF temp file
        txtFile = File.createTempFile("test-document", ".txt");
        try (FileOutputStream fos = new FileOutputStream(txtFile)) {
            fos.write("not a pdf".getBytes(StandardCharsets.UTF_8));
        }
        txtFile.deleteOnExit();
    }

    @AfterAll
    void teardown() {
        ApplicationConfig.stopServer(app);
    }

    // ---------------------------
    // TASK-2: Auto journal creation on register
    // ---------------------------

    @Test
    @Order(1)
    void register_adminRole_journalCreated() {
        String email = "task2_admin_" + System.currentTimeMillis() + "@test.com";

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "name": "Task2 Admin",
                        "email": "%s",
                        "password": "pass123",
                        "displayName": "Task2 Admin",
                        "displayEmail": "%s",
                        "displayPhone": "111-0000-0001",
                        "internalEmail": "task2admin.internal@test.com",
                        "internalPhone": "222-0000-0001",
                        "role": "ADMIN"
                    }
                """, email, email))
                .post("/auth/register")
                .then()
                .statusCode(201);

        int userId = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/users/by-email/" + email)
                .then()
                .statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/staff-journals/user/" + userId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId));
    }

    @Test
    @Order(2)
    void register_careworkerRole_journalCreated() {
        String email = "task2_cw_" + System.currentTimeMillis() + "@test.com";

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "name": "Task2 Careworker",
                        "email": "%s",
                        "password": "pass123",
                        "displayName": "Task2 CW",
                        "displayEmail": "%s",
                        "displayPhone": "111-0000-0002",
                        "internalEmail": "task2cw.internal@test.com",
                        "internalPhone": "222-0000-0002",
                        "role": "CAREWORKER"
                    }
                """, email, email))
                .post("/auth/register")
                .then()
                .statusCode(201);

        int userId = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/users/by-email/" + email)
                .then()
                .statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/staff-journals/user/" + userId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId));
    }

    @Test
    @Order(3)
    void register_guardianRole_noJournalCreated() {
        String email = "task2_guardian_" + System.currentTimeMillis() + "@test.com";

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "name": "Task2 Guardian",
                        "email": "%s",
                        "password": "pass123",
                        "displayName": "Task2 Guardian",
                        "displayEmail": "%s",
                        "displayPhone": "111-0000-0003",
                        "internalEmail": "task2guardian.internal@test.com",
                        "internalPhone": "222-0000-0003",
                        "role": "GUARDIAN"
                    }
                """, email, email))
                .post("/auth/register")
                .then()
                .statusCode(201);

        int userId = given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/users/by-email/" + email)
                .then()
                .statusCode(200)
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/staff-journals/user/" + userId)
                .then()
                .statusCode(404);
    }

    // ---------------------------
    // TASK-3: Entries CRUD
    // ---------------------------

    @Test
    @Order(4)
    void createEntry_validInput_returns201WithTitleAndId() {
        createdEntryId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Første notat\",\"content\":\"Testindhold\"}")
                .post("/staff-journals/" + crudJournalId + "/entries")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("title", equalTo("Første notat"))
                .body("id", notNullValue())
                .extract().path("id");
    }

    @Test
    @Order(5)
    void getEntries_returns200WithList() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/staff-journals/" + crudJournalId + "/entries")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(6)
    void updateEntry_changesTitle_returns200() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Opdateret notat\",\"content\":\"Nyt indhold\"}")
                .put("/staff-journals/" + crudJournalId + "/entries/" + createdEntryId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Opdateret notat"));
    }

    @Test
    @Order(7)
    void deleteEntry_returns204() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .delete("/staff-journals/" + crudJournalId + "/entries/" + createdEntryId)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(8)
    void createEntry_emptyTitle_returns400() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"title\":\"\",\"content\":\"Noget indhold\"}")
                .post("/staff-journals/" + crudJournalId + "/entries")
                .then()
                .statusCode(400);
    }

    // ---------------------------
    // TASK-3: Contracts
    // ---------------------------

    @Test
    @Order(9)
    void uploadContract_validPdf_returns201WithFilename() {
        createdContractId = given()
                .header("Authorization", "Bearer " + adminToken)
                .multiPart("file", pdfFile, "application/pdf")
                .post("/staff-journals/" + crudJournalId + "/contract")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("filename", notNullValue())
                .body("id", notNullValue())
                .extract().path("id");
    }

    @Test
    @Order(10)
    void uploadContract_nonPdfFile_returns400() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .multiPart("file", txtFile, "text/plain")
                .post("/staff-journals/" + crudJournalId + "/contract")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(11)
    void uploadContract_noFile_returns400() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .post("/staff-journals/" + crudJournalId + "/contract")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(12)
    void downloadContract_exists_returns200WithPdfContentType() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/staff-journals/" + crudJournalId + "/contract/" + createdContractId)
                .then()
                .statusCode(200)
                .contentType("application/pdf");
    }

    @Test
    @Order(13)
    void deleteContract_returns204() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .delete("/staff-journals/" + crudJournalId + "/contract/" + createdContractId)
                .then()
                .statusCode(204);
    }
}
