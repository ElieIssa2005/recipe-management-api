package com.example.recipeoop_1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // Import this

@SpringBootTest
@ActiveProfiles("test") // Add this annotation
class RecipeOop1ApplicationTests {

    @Test
    void contextLoads() {
        // This test will now attempt to load the context with application-test.properties
    }

}