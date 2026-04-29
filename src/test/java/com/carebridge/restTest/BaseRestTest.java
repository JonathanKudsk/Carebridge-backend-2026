package com.carebridge.restTest;

import com.carebridge.CareBridgeApplication;
import com.carebridge.config.Populator;
import com.carebridge.dao.impl.UserDAO;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CareBridgeApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class BaseRestTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected Populator populator;

    @Autowired
    protected UserDAO userDAO;

    protected String adminToken;
    protected String userToken;

    private static final java.util.concurrent.atomic.AtomicLong sequence = new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());

    protected long nextId() {
        return sequence.incrementAndGet();
    }

    @BeforeAll
    public void setupBase() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        
        populator.populate();
        // Get tokens
        adminToken = login("admin@carebridge.io", "admin");

        // Ensure Alice exists and get token
        String aliceEmail = "alice" + nextId() + "@carebridge.io";
        ensureUserExists("Alice", aliceEmail, "password123");
        userToken = login(aliceEmail, "password123");
    }

    protected String login(String email, String password) {
        return given()
                .contentType("application/json")
                .body(java.util.Map.of("email", email, "password", password))
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    protected void ensureUserExists(String name, String email, String password) {
        // Try to login first, if fails, register
        int status = given()
                .contentType("application/json")
                .body(java.util.Map.of("email", email, "password", password))
                .post("/api/auth/login")
                .getStatusCode();
        
        if (status != 200) {
            given()
                .contentType("application/json")
                .body(java.util.Map.of(
                    "name", name, 
                    "email", email, 
                    "password", password, 
                    "role", "USER"
                ))
                .post("/api/auth/register");
        }
    }
}
