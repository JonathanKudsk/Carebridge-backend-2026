package restTest;

import com.carebridge.config.ApplicationConfig;
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
import com.carebridge.dtos.UserWithLocationDTO;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import populator.LocationPopulator;

import static io.restassured.RestAssured.given;

public class UserTest {
    private static Javalin app;
    static private EntityManagerFactory emf;
    private static String authToken;
    private static String adminAuthToken;

    @BeforeAll
    static void setupOnce() {

        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(7007);
        RestAssured.baseURI = "http://localhost:7007/api";
    }

    @BeforeEach
    void setup() {
        Populator.populate(emf);

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

    @AfterEach
        //delete all data
    void teardown() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        //delete everything, replace star with tables
        em.createNativeQuery("TRUNCATE TABLE locations, cities, users RESTART IDENTITY CASCADE")
                .executeUpdate();
        em.getTransaction().commit();

        em.close();
    }

    @AfterAll
    static void tearDownOnce() {
        ApplicationConfig.stopServer(app);
    }

    @Test
    void readWithLocation() {
        LocationPopulator.populate();

        UserWithLocationDTO expected = new UserWithLocationDTO();

        UserWithLocationDTO actual = given().
                when()
                .get("1/locations/")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", UserWithLocationDTO.class);


        //assertEquals(actual.size(), expected.size());
        //assertTrue(actual.containsAll(expected));
    }
}
