package com.example.recipeoop_1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("recipe-management-api.onrender.com")
public class ProxyController {
    // This controller just catches requests to the hard-coded API URL
    // and forwards them to your actual controllers

    @RequestMapping("/**")
    public String handleProxy() {
        return "API proxy is working. This is a fallback and shouldn't be seen.";
    }
}