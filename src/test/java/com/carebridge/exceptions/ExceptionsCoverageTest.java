package com.carebridge.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionsCoverageTest {

    @Test
    void testApiException() {
        ApiException ex = new ApiException(400, "Bad Request");
        assertEquals(400, ex.getStatusCode());
        assertEquals("Bad Request", ex.getMessage());
    }

    @Test
    void testApiRuntimeException() {
        ApiRuntimeException ex = new ApiRuntimeException(500, "Internal Error");
        assertEquals(500, ex.getStatusCode());
        assertEquals("Internal Error", ex.getMessage());
    }

    @Test
    void testNotAuthorizedException() {
        NotAuthorizedException ex1 = new NotAuthorizedException(401, "Unauthorized");
        assertEquals(401, ex1.getStatusCode());
        assertEquals("Unauthorized", ex1.getMessage());

        Exception cause = new RuntimeException("cause");
        NotAuthorizedException ex2 = new NotAuthorizedException(403, "Forbidden", cause);
        assertEquals(403, ex2.getStatusCode());
        assertEquals("Forbidden", ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }

    @Test
    void testValidationException() {
        ValidationException ex = new ValidationException("Validation failed");
        assertEquals("Validation failed", ex.getMessage());
    }

    @Test
    void testGlobalExceptionHandlerMoreBranches() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // handleNotFound with null message
        ResponseEntity<?> resp1 = handler.handleNotFound(new EntityNotFoundException((String) null));
        assertEquals(HttpStatus.NOT_FOUND, resp1.getStatusCode());
        assertEquals("Not found", ((Map<?, ?>) resp1.getBody()).get("error"));

        // handleNotFound with JpaObjectRetrievalFailureException
        ResponseEntity<?> resp1b = handler.handleNotFound(new org.springframework.orm.jpa.JpaObjectRetrievalFailureException(new EntityNotFoundException("NF")));
        assertEquals(HttpStatus.NOT_FOUND, resp1b.getStatusCode());
        assertEquals("NF", ((Map<?, ?>) resp1b.getBody()).get("error"));

        // handleApiRuntime with null message
        ResponseEntity<?> resp2 = handler.handleApiRuntime(new ApiRuntimeException(400, null));
        assertEquals(HttpStatus.BAD_REQUEST, resp2.getStatusCode());
        assertEquals("API Error", ((Map<?, ?>) resp2.getBody()).get("error"));

        // handleRuntime with null message
        ResponseEntity<?> resp3 = handler.handleRuntime(new RuntimeException((String) null));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp3.getStatusCode());
        assertEquals("Internal Server Error", ((Map<?, ?>) resp3.getBody()).get("error"));
        
        // handleRuntime with "not found" in message
        ResponseEntity<?> resp4 = handler.handleRuntime(new RuntimeException("Resource Not Found"));
        assertEquals(HttpStatus.NOT_FOUND, resp4.getStatusCode());
        assertEquals("Resource Not Found", ((Map<?, ?>) resp4.getBody()).get("error"));

        // handleRuntime with "not found" in message (lowercase)
        ResponseEntity<?> resp5 = handler.handleRuntime(new RuntimeException("something was not found"));
        assertEquals(HttpStatus.NOT_FOUND, resp5.getStatusCode());
    }
}
