package com.carebridge.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({EntityNotFoundException.class, JpaObjectRetrievalFailureException.class})
    public ResponseEntity<?> handleNotFound(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Not found";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
    }

    @ExceptionHandler(ApiRuntimeException.class)
    public ResponseEntity<?> handleApiRuntime(ApiRuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "API Error";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", msg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Internal Server Error";
        if (msg.toLowerCase().contains("not found") || msg.toLowerCase().contains("resource not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
    }
}
