package com.example.recipeoop_1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Recipe API is running");
        response.put("info", "Use /index.html to access the Recipe Management System");
        response.put("timestamp", new java.util.Date());
        return ResponseEntity.ok(response);
    }
}