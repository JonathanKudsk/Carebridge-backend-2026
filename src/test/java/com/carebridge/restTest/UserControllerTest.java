package com.carebridge.restTest;

import com.carebridge.entities.User;
import com.carebridge.entities.Resident;
import com.carebridge.enums.Role;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest extends BaseRestTest {

    private static Long createdUserId;
    private static String createdEmail;

    @Test
    @Order(1)
    public void testReadAllUsers() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(2)
    public void testCreateUser() {
        createdEmail = "newuser" + nextId() + "@example.com";
        Map<String, Object> userMap = Map.of(
                "name", "New User",
                "email", createdEmail,
                "password", "password123",
                "role", "USER"
        );

        Object idObj = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(userMap)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .body("email", equalTo(createdEmail))
                .extract().path("id");
        createdUserId = ((Number) idObj).longValue();
        
        // Branch: no password
        Map<String, Object> noPassMap = Map.of(
                "name", "No Pass",
                "email", "nopass" + nextId() + "@test.com",
                "role", "USER"
        );
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(noPassMap)
                .when()
                .post("/api/users")
                .then()
                .statusCode(500); 
    }

    @Test
    @Order(3)
    public void testReadUser() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users/" + createdUserId)
                .then()
                .statusCode(200);
        
        // Branch: user not found
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users/999999")
                .then()
                .statusCode(400); 
    }

    @Test
    @Order(4)
    public void testUpdateUser() {
        Map<String, Object> updateMap = Map.of(
                "name", "Updated Name",
                "password", "newpassword"
        );
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(updateMap)
                .when()
                .put("/api/users/" + createdUserId)
                .then()
                .statusCode(200);

        // Branch: no password
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Other Name"))
                .when()
                .put("/api/users/" + createdUserId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(5)
    public void testMe() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200);
        
        // Branch: jwtUser == null
        given()
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(400); 
        
        // Branch: user not found in DB
        String tempEmail = "me-nf-" + nextId() + "@test.com";
        ensureUserExists("Temp", tempEmail, "pass");
        String tempToken = login(tempEmail, "pass");
        User tempUser = userDAO.readByEmail(tempEmail);
        userDAO.delete(tempUser.getId());
        
        given()
                .header("Authorization", "Bearer " + tempToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(404);
        
        userDAO.delete(createdUserId);
    }

    @Test
    @Order(6)
    public void testLinkResidents() {
        String joeEmail = "joe-link-" + nextId() + "@example.com";
        Map<String, Object> guardianMap = Map.of(
                "name", "Guardian Joe",
                "email", joeEmail,
                "password", "password123",
                "role", "GUARDIAN"
        );

        Object jId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(guardianMap)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract().path("id");
        Long joeId = ((Number) jId).longValue();

        // Create a resident to link
        Map<String, Object> residentReq = Map.of(
            "firstName", "Børge",
            "lastName", "Børgesen",
            "cprNr", "CPR-" + nextId()
        );

        Object rId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(residentReq)
                .when()
                .post("/api/residents/create")
                .then()
                .statusCode(201)
                .extract().path("id");
        Long residentId = ((Number) rId).longValue();

        // Case: valid link
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("residentIds", List.of(residentId)))
                .when()
                .post("/api/users/" + joeId + "/link-residents")
                .then()
                .statusCode(200);
        
        // Case: invalid resident ID (hits r != null false branch, still returns 200)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("residentIds", List.of(999999L)))
                .when()
                .post("/api/users/" + joeId + "/link-residents")
                .then()
                .statusCode(200);
                
        // Branch: user == null
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(Map.of("residentIds", List.of(residentId)))
                .when()
                .post("/api/users/999999/link-residents")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    public void testPopulate() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/api/users/populate")
                .then()
                .statusCode(200)
                .body("msg", containsString("Database populated"));
    }

    @Test
    @Order(8)
    public void testDeleteUser() {
        String delEmail = "del-" + nextId() + "@test.com";
        ensureUserExists("Del", delEmail, "p");
        User u = userDAO.readByEmail(delEmail);
        
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/users/" + u.getId())
                .then()
                .statusCode(204);
    }
}
