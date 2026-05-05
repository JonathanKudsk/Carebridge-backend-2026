package restTest;

import com.carebridge.config.Populator;
import com.carebridge.dtos.TemplateDetailedResponseDTO;
import com.carebridge.dtos.TemplateResponseDTO;
import com.carebridge.entities.Field;
import com.carebridge.entities.Template;
import com.carebridge.enums.FieldType;
import populator.TemplatePopulator;
import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import populator.TemplatePopulator;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class TemplateTest {
    private static Javalin app;
    static private EntityManagerFactory emf;
    private static String authToken;
    private static String adminAuthToken;

    @BeforeAll
    static void setupOnce() {

        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(7007);
        Populator.populate(emf);

        RestAssured.baseURI = "http://localhost:7007/api";

        authToken = given()
                .contentType(io.javalin.http.ContentType.JSON)
                .body("{\"email\":\"alice@carebridge.io\", \"password\":\"password123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");

        adminAuthToken = given()
                .contentType(io.javalin.http.ContentType.JSON)
                .body("{\"email\":\"admin@carebridge.io\", \"password\":\"admin123\"}")
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    @BeforeEach
    void setup() {
        TemplatePopulator.populate();
    }

    @AfterEach
        //delete all data
    void teardown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        //delete everything, replace star with tables
        em.createNativeQuery("TRUNCATE TABLE templates, fields RESTART IDENTITY CASCADE")
                .executeUpdate();
        em.getTransaction().commit();

        em.close();
    }

    @AfterAll
    static void tearDownOnce() {
        ApplicationConfig.stopServer(app);
    }


    @Test
    void read() {
        TemplateDetailedResponseDTO expected = new TemplateDetailedResponseDTO(TemplatePopulator.fetch().get(0));

        TemplateDetailedResponseDTO actual = given().
                when()
                .get("/templates/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", TemplateDetailedResponseDTO.class);

        assertEquals(actual, expected);
    }

    @Test
    void readAll() {
        List<TemplateResponseDTO> expected = TemplatePopulator.fetch().stream().map(TemplateResponseDTO::new).toList();

        List<TemplateResponseDTO> actual = given().
                when()
                .get("/templates/")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getList("$", TemplateResponseDTO.class);

        assertEquals(actual.size(), expected.size());
        assertTrue(actual.containsAll(expected));
    }

    @Test
    void create() {
        Template test = Template
                .builder()
                .title("test")
                .build();
        test.addField(Field.builder().fieldType(FieldType.TEXTFIELD).title("test").build());
        test.addField(Field.builder().fieldType(FieldType.CHECKBOX).title("test").build());

        TemplateDetailedResponseDTO expected = new TemplateDetailedResponseDTO(test);

        String body =
                "{\n" +
                        "  \"title\": \"test\",\n" +
                        "  \"fields\": [\n" +
                        "    {\n" +
                        "      \"title\": \"test\",\n" +
                        "      \"fieldType\": \"TEXTFIELD\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"title\": \"test\",\n" +
                        "      \"fieldType\": \"CHECKBOX\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

        TemplateDetailedResponseDTO added = given()
                .header("Authorization", "Bearer " + adminAuthToken) // Admin role allows Admin token
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .when()
                .post("/templates/")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", TemplateDetailedResponseDTO.class);

        assertEquals(added, expected);

        TemplateDetailedResponseDTO actual = (TemplateDetailedResponseDTO) given().
                when()
                .get("/templates/" + added.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", TemplateDetailedResponseDTO.class);

        assertEquals(actual, expected);
    }

    @Test
    void delete() {

        TemplateDetailedResponseDTO deleted = new TemplateDetailedResponseDTO(TemplatePopulator.fetch().get(1));

        given()
                .header("Authorization", "Bearer " + adminAuthToken) // Admin role allows Admin token
                .when()
                .delete("/templates/1")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + authToken) // ANYONE role allows USER token
                .when()
                .get("/templates/1")
                .then()
                .statusCode(404);
    }
}
