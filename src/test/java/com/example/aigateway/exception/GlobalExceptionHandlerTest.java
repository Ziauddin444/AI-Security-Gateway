package com.example.aigateway.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("should return 403 with BLOCKED status for PromptInjectionException")
    void handlePromptInjection() {
        PromptInjectionException ex = new PromptInjectionException("Attack detected");

        ResponseEntity<Map<String, Object>> response = handler.handlePromptInjection(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("BLOCKED", response.getBody().get("status"));
        assertEquals("Attack detected", response.getBody().get("reason"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("should return 500 with ERROR status for generic Exception")
    void handleGenericException() {
        Exception ex = new RuntimeException("Something broke");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERROR", response.getBody().get("status"));
        assertEquals("An unexpected error occurred: Something broke", response.getBody().get("reason"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}
