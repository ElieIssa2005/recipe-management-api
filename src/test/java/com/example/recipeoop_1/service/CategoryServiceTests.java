package com.example.recipeoop_1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link CategoryService}.
 * These tests focus on the logic within CategoryService, mocking external dependencies like MongoTemplate.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTests {

    // Mock the MongoTemplate dependency, as we don't want to interact with a real database in unit tests.
    @Mock
    private MongoTemplate mongoTemplate;

    // Inject the mocks into the CategoryService instance.
    @InjectMocks
    private CategoryService categoryService;

    /**
     * Test for {@link CategoryService#getAllCategories()} when recipe collections exist.
     * Verifies that collection names are correctly filtered and mapped to category names.
     */
    @Test
    void testGetAllCategories_Success() {
        // Arrange: Define the mock behavior for mongoTemplate.getCollectionNames()
        // This simulates the MongoDB returning a set of collection names.
        Set<String> collectionNames = Set.of("recipe_desserts", "recipe_main_course", "users", "system.indexes");
        when(mongoTemplate.getCollectionNames()).thenReturn(collectionNames);

        // Act: Call the method under test.
        List<String> categories = categoryService.getAllCategories();

        // Assert: Verify the results.
        // We expect only "desserts" and "main_course" to be returned.
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertTrue(categories.contains("desserts"));
        assertTrue(categories.contains("main_course"));
        assertFalse(categories.contains("users")); // Ensure non-recipe collections are filtered out
    }

    /**
     * Test for {@link CategoryService#getAllCategories()} when no collections match the "recipe_" prefix.
     * Verifies that an empty list is returned.
     */
    @Test
    void testGetAllCategories_NoRecipeCollections() {
        // Arrange: Simulate MongoDB returning collection names that don't match the recipe prefix.
        Set<String> collectionNames = Set.of("users", "profiles", "system.indexes");
        when(mongoTemplate.getCollectionNames()).thenReturn(collectionNames);

        // Act: Call the method under test.
        List<String> categories = categoryService.getAllCategories();

        // Assert: Verify that an empty list is returned.
        assertNotNull(categories);
        assertTrue(categories.isEmpty());
    }

    /**
     * Test for {@link CategoryService#getAllCategories()} when MongoDB returns an empty set of collections.
     * Verifies that an empty list is returned.
     */
    @Test
    void testGetAllCategories_EmptyCollections() {
        // Arrange: Simulate MongoDB returning no collections.
        when(mongoTemplate.getCollectionNames()).thenReturn(Collections.emptySet());

        // Act: Call the method under test.
        List<String> categories = categoryService.getAllCategories();

        // Assert: Verify that an empty list is returned.
        assertNotNull(categories);
        assertTrue(categories.isEmpty());
    }

    /**
     * Test for {@link CategoryService#ensureCategoryExists(String)} when the collection does not exist.
     * Verifies that {@link MongoTemplate#createCollection(String)} is called.
     */
    @Test
    void testEnsureCategoryExists_CreatesNewCollection() {
        // Arrange: Define the category and the expected formatted collection name.
        String category = "New Category";
        String expectedCollectionName = "recipe_new_category"; // Based on formatCollectionName logic

        // Simulate that the collection does not exist.
        when(mongoTemplate.collectionExists(expectedCollectionName)).thenReturn(false);

        // Act: Call the method under test.
        categoryService.ensureCategoryExists(category);

        // Assert: Verify that createCollection was called exactly once with the correct name.
        verify(mongoTemplate, times(1)).createCollection(expectedCollectionName);
    }

    /**
     * Test for {@link CategoryService#ensureCategoryExists(String)} when the collection already exists.
     * Verifies that {@link MongoTemplate#createCollection(String)} is NOT called.
     */
    @Test
    void testEnsureCategoryExists_DoesNotCreateExistingCollection() {
        // Arrange: Define the category and the expected formatted collection name.
        String category = "Existing Category";
        String expectedCollectionName = "recipe_existing_category";

        // Simulate that the collection already exists.
        when(mongoTemplate.collectionExists(expectedCollectionName)).thenReturn(true);

        // Act: Call the method under test.
        categoryService.ensureCategoryExists(category);

        // Assert: Verify that createCollection was NOT called.
        verify(mongoTemplate, never()).createCollection(expectedCollectionName);
    }

    /**
     * Test for the static method {@link CategoryService#formatCollectionName(String)} with various inputs.
     * Verifies correct formatting of category names to collection names.
     */
    @Test
    void testFormatCollectionName_VariousInputs() {
        // Test with a typical category name.
        assertEquals("recipe_main_course", CategoryService.formatCollectionName("Main Course"));
        // Test with leading/trailing spaces and mixed case.
        assertEquals("recipe_dessert_time", CategoryService.formatCollectionName("  Dessert Time  "));
        // Test with multiple spaces between words.
        assertEquals("recipe_quick_snacks", CategoryService.formatCollectionName("Quick  Snacks"));
        // Test with a single word category.
        assertEquals("recipe_soups", CategoryService.formatCollectionName("Soups"));
        // Test with an already lowercase and underscored category.
        assertEquals("recipe_already_formatted", CategoryService.formatCollectionName("already_formatted"));
        // Test with null input, should default to "uncategorized".
        assertEquals("recipe_uncategorized", CategoryService.formatCollectionName(null));
        // Test with empty string input, should default to "uncategorized".
        assertEquals("recipe_uncategorized", CategoryService.formatCollectionName(""));
        // Test with blank string (only spaces) input, should default to "uncategorized".
        assertEquals("recipe_uncategorized", CategoryService.formatCollectionName("   "));
    }
}