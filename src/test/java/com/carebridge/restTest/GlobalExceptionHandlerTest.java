package com.carebridge.restTest;

import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @Order(1)
    void testHandleNotFound() {
        EntityNotFoundException enf = new EntityNotFoundException("NF");
        ResponseEntity<?> resp = handler.handleNotFound(enf);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        
        ResponseEntity<?> resp2 = handler.handleNotFound(new JpaObjectRetrievalFailureException(enf));
        assertEquals(HttpStatus.NOT_FOUND, resp2.getStatusCode());
    }

    @Test
    @Order(2)
    void testHandleApiRuntime() {
        ResponseEntity<?> resp = handler.handleApiRuntime(new ApiRuntimeException(403, "Forbidden"));
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    @Order(3)
    void testHandleRuntime() {
        // Not found branch
        ResponseEntity<?> resp = handler.handleRuntime(new RuntimeException("resource not found"));
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());

        // Internal error branch
        ResponseEntity<?> resp2 = handler.handleRuntime(new RuntimeException("Something else"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp2.getStatusCode());
        
        // Null message check
        // The handler does: if (ex.getMessage() != null && ...)
        // So if getMessage() is null, it should return 500 without NPE
        ResponseEntity<?> resp3 = handler.handleRuntime(new RuntimeException((String)null));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp3.getStatusCode());
    }
}
