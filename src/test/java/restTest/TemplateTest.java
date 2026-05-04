package restTest;

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

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
class TemplateTest {
    private static Javalin app;
    static private EntityManagerFactory emf;

    @BeforeAll
    static void setupOnce() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        TemplatePopulator.setEMF(emf);
        //ExampleController.setEmf(emf);

        app = ApplicationConfig.startServer(7007);
        RestAssured.baseURI = "http://localhost:7007/api/templates";
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
        em.createNativeQuery("TRUNCATE TABLE * RESTART IDENTITY CASCADE")
                .executeUpdate();
        em.getTransaction().commit();

        em.close();
    }

    @AfterAll
    static void tearDownOnce() {
        ApplicationConfig.stopServer(app);
        if (emf != null) emf.close();
    }


    @Test
    void read() {
        Object expected = new Object();

        Object actual = given().
                when()
                .get("/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", Object.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void readAll() {
        Object[] expected = new Object[]{};

        List<Object> actual = given().
                when()
                .get("/")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getList("$", Object.class);

        assertThat(actual, containsInAnyOrder(expected));
    }

    @Test
    void create() {

        Object expected = new Object();

        String body =
                "json structured after input DTO";

        Object added = given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", Object.class);

        assertThat(added,equalTo(expected));

        Object actual = given().
                when()
                .get("/4")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", Object.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void update() {
        Object expected = new Object();

        String body =
                "json structured after input DTO";

        Object added = given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .when()
                .put("/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", Object.class);

        assertThat(added,equalTo(expected));

        Object actual = given().
                when()
                .get("/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getObject("$", Object.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void delete() {

        Object deleted = new Object();

        given().
                when()
                .delete("/1")
                .then()
                .statusCode(204);

        Object[] expected = new Object[]{};

        List<Object> actual = given().
                when()
                .get("/")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().body().jsonPath().getList("$", Object.class);

        assertThat(actual, containsInAnyOrder(expected));

        assertThat(actual, everyItem(not((deleted))));
    }
}

