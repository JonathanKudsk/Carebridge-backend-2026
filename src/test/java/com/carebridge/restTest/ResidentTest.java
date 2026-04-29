package com.carebridge.restTest;

import com.carebridge.entities.Resident;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentTest extends BaseRestTest {

    private static int createdId;
    private static String cpr;

    @Test
    @Order(1)
    public void testCreateResident() {
        cpr = "RES" + nextId();
        Map<String, Object> payload = Map.of(
            "firstName", "Test",
            "lastName", "Resident",
            "cprNr", cpr
        );

        createdId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        // Branch: no jwtUser
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "Non", "lastName", "Auth", "cprNr", "NOAUTH" + nextId()))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201);
        
        // Branch: jwtUser present but user not in DB (trigger user == null branch)
        String tempEmail = "res-link-nf-" + nextId() + "@test.com";
        ensureUserExists("Ev", tempEmail, "p");
        String tempToken = login(tempEmail, "p");
        userDAO.delete(userDAO.readByEmail(tempEmail).getId());
        
        given()
                .header("Authorization", "Bearer " + tempToken)
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "FN", "lastName", "LN", "cprNr", "C" + nextId()))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(2)
    public void testReadAllResidents() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    public void testReadResidentById() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents/" + createdId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdId));
        
        // Branch: not found (trigger catch)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(4)
    public void testReadByCpr() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents/cpr/" + cpr)
                .then()
                .statusCode(200)
                .body("cprNr", equalTo(cpr));
        
        // Branch: not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/residents/cpr/NOTFOUND")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    public void testResidentErrors() {
        // Branch: firstName blank
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("lastName", "L", "cprNr", "C"))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(400);

        // Branch: lastName blank
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "F", "cprNr", "C"))
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(400);
    }
}
