package com.example.recipeoop_1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller that handles requests to the root path ("/") of the application.
 * <p>
 * It provides a simple JSON response indicating the status of the API
 * and a welcome message.
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.1
 * @since 2025-05-14
 */
@RestController
public class WelcomeController {

    /**
     * Handles GET requests to the root path ("/") of the application.
     * <p>
     * Returns a JSON response containing the API status, a welcome message,
     * informational text, and a timestamp.
     * </p>
     * HTTP Method: GET
     * Path: /
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Always returns a JSON object with the following structure:
     * <pre>{@code
     * {
     * "status": "UP",
     * "message": "Recipe API is running",
     * "info": "Use /index.html to access the Recipe Management System", // Consider updating this message
     * "timestamp": "current_date_time"
     * }
     * }</pre>
     * Note: The "info" message currently points to "/index.html".
     * This should ideally be updated to point to the Javadoc documentation at "/apidocs/index.html"
     * if the old static index.html has been removed. Alternatively, this endpoint could
     * be modified to redirect to "/apidocs/index.html".
     * </li>
     * </ul>
     *
     * @return A {@link ResponseEntity} containing a map with status and informational messages,
     * and HTTP status 200 (OK).
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Recipe API is running");
        // The 'info' message below might be outdated if static/index.html (old UI) was removed.
        // Consider changing this to point to "/apidocs/index.html" for the Javadoc.
        response.put("info", "Use /index.html to access the Recipe Management System");
        response.put("timestamp", new java.util.Date());
        return ResponseEntity.ok(response);
    }
}