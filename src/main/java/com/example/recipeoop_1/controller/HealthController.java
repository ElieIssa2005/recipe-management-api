package com.example.recipeoop_1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller providing a health check endpoint for the application.
 * <p>
 * This controller is used to verify the operational status of the Recipe API.
 * It's a common practice for monitoring and ensuring the application is running correctly.
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.0
 * @since 2025-05-14
 */
@RestController
public class HealthController {

    /**
     * Performs a health check of the Recipe API.
     * <p>
     * This endpoint responds to GET requests at {@code /health}. It returns a JSON object
     * indicating the status of the service, the service name, and a timestamp.
     * This endpoint is typically public and does not require authentication.
     * </p>
     * HTTP Method: GET
     * Path: /health
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: The service is up and running. The response body will be a JSON object:
     * <pre>{@code
     * {
     * "status": "UP",
     * "service": "Recipe API",
     * "timestamp": "current_date_time"
     * }
     * }</pre>
     * </li>
     * </ul>
     *
     * @return A {@link ResponseEntity} containing a map with health status information
     * and HTTP status 200 (OK).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP"); // Indicates the service is operational
        status.put("service", "Recipe API"); // Identifies the service
        status.put("timestamp", new java.util.Date()); // Provides the current server time
        return ResponseEntity.ok(status);
    }
}