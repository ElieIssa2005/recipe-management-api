package com.example.recipeoop_1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

@Controller
public class HtmlController {

    @GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String serveIndex() throws IOException {
        return loadHtmlFile("static/index.html");
    }

    @GetMapping(path = "/customer.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String serveCustomer() throws IOException {
        return loadHtmlFile("static/customer.html");
    }

    @GetMapping(path = "/chef.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String serveChef() throws IOException {
        return loadHtmlFile("static/chef.html");
    }

    private String loadHtmlFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}