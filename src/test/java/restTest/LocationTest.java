package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
import com.carebridge.dtos.LocationResponseDTO;
import com.carebridge.dtos.TemplateDetailedResponseDTO;
import com.carebridge.entities.City;
import com.carebridge.entities.Location;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import populator.LocationPopulator;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class LocationTest {
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
        LocationPopulator.populate();
    }

    @AfterEach
        //delete all data
    void teardown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        //delete everything, replace star with tables
        em.createNativeQuery("TRUNCATE TABLE locations, cities RESTART IDENTITY CASCADE")
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
        LocationResponseDTO expected = new LocationResponseDTO(LocationPopulator.fetch().get(0));

        LocationResponseDTO actual = given().
                when()
                .get("/locations/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", LocationResponseDTO.class);

        assertEquals(actual, expected);
    }

    @Test
    void readAll() {
        List<LocationResponseDTO> expected = LocationPopulator.fetch().stream().map(LocationResponseDTO::new).toList();

        List<LocationResponseDTO> actual = given().
                when()
                .get("/locations/")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getList("$", LocationResponseDTO.class);

        assertEquals(actual.size(), expected.size());
        assertTrue(actual.containsAll(expected));
    }

    @Test
    void create() {
        Location test = Location.builder()
                .id(4)
                .locationName("test")
                .address("test")
                .build();
        test.setCity(City.builder()
                        .id(3)
                        .postalcode(1211)
                        .cityname("testBy")
                .build());

        LocationResponseDTO expected = new LocationResponseDTO(test);

        String body =
                "{\n" +
                        "  \"locationName\": \"test\",\n" +
                        "  \"address\": \"test\",\n" +
                        "  \"city\":\n" +
                        "  {\n" +
                        "    \"cityName\": \"testBy\",\n" +
                        "    \"postalCode\": \"1211\"\n" +
                        "  }\n" +
                        "}";

        LocationResponseDTO added = given()
                .header("Authorization", "Bearer " + adminAuthToken) // Admin role allows Admin token
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .when()
                .post("/locations/")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", LocationResponseDTO.class);

        assertEquals(added, expected);

        LocationResponseDTO actual = (LocationResponseDTO) given().
                when()
                .get("/locations/" + added.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", LocationResponseDTO.class);

        assertEquals(actual, expected);
    }

    @Test
    void Update() {
        Location test = Location.builder()
                .id(1L)
                .locationName("update")
                .address("update")
                .build();
        test.setCity(City.builder()
                .id(3)
                .postalcode(1211)
                .cityname("testBy")
                .build());

        LocationResponseDTO expected = new LocationResponseDTO(test);

        String body =
                "{\n" +
                        "  \"locationName\": \"update\",\n" +
                        "  \"address\": \"update\",\n" +
                        "  \"city\":\n" +
                        "  {\n" +
                        "    \"cityName\": \"testBy\",\n" +
                        "    \"postalCode\": \"1211\"\n" +
                        "  }\n" +
                        "}";

        LocationResponseDTO added = given()
                .header("Authorization", "Bearer " + adminAuthToken) // Admin role allows Admin token
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .when()
                .put("/locations/1")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", LocationResponseDTO.class);

        assertEquals(added, expected);

        LocationResponseDTO actual = (LocationResponseDTO) given().
                when()
                .get("/locations/" + added.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", LocationResponseDTO.class);

        assertEquals(actual, expected);
    }

    @Test
    void delete() {
        given()
                .header("Authorization", "Bearer " + adminAuthToken) // Admin role allows Admin token
                .when()
                .delete("/locations/1")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + authToken) // ANYONE role allows USER token
                .when()
                .get("/locations/1")
                .then()
                .statusCode(404);
    }
}