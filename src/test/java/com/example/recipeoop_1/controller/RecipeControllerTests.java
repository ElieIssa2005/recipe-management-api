package com.example.recipeoop_1.controller;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.service.CategoryService;
import com.example.recipeoop_1.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link RecipeController}.
 * Tests CRUD operations, search functionalities, and security for recipe endpoints.
 * Uses an embedded MongoDB instance.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Optional: if you have application-test.properties
class RecipeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecipeService recipeService; // Used for setting up test data

    @Autowired
    private CategoryService categoryService; // Used for setting up test data

    @Autowired
    private MongoTemplate mongoTemplate; // Used for direct DB cleanup

    private Recipe testRecipe1, testRecipe2;
    private final String userUsername = "testuser";
    private final String adminUsername = "testadmin";

    @BeforeEach
    void setUp() {
        // Clean up database before each test to ensure isolation
        cleanUpDatabase();

        // Create some test recipes directly using the service
        // This ensures categories and collections are also created by CategoryService logic
        Recipe recipeToSave1 = new Recipe();
        recipeToSave1.setTitle("Lemon Tart");
        recipeToSave1.setCategory("Desserts");
        recipeToSave1.setIngredients(Arrays.asList("Lemon", "Sugar", "Pastry"));
        recipeToSave1.setInstructions("Bake the tart.");
        recipeToSave1.setCookingTime(45);
        testRecipe1 = recipeService.createRecipe(recipeToSave1, userUsername); // Created by 'testuser'

        Recipe recipeToSave2 = new Recipe();
        recipeToSave2.setTitle("Chicken Soup");
        recipeToSave2.setCategory("Soups");
        recipeToSave2.setIngredients(Arrays.asList("Chicken", "Broth", "Vegetables"));
        recipeToSave2.setInstructions("Simmer the soup.");
        recipeToSave2.setCookingTime(90);
        testRecipe2 = recipeService.createRecipe(recipeToSave2, adminUsername); // Created by 'testadmin'
    }

    @AfterEach
    void tearDown() {
        // Clean up database after each test
        cleanUpDatabase();
    }

    private void cleanUpDatabase() {
        // Drop all collections that start with "recipe_"
        mongoTemplate.getCollectionNames().stream()
                .filter(name -> name.startsWith("recipe_"))
                .forEach(mongoTemplate::dropCollection);
    }

    // --- Test Get All Categories ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"}) // Simulate a logged-in user with ROLE_USER
    void testGetAllCategories_AsUser_Success() throws Exception {
        mockMvc.perform(get("/api/recipes/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2)))) // Expect at least "Desserts" and "Soups"
                .andExpect(jsonPath("$", hasItem("Desserts")))
                .andExpect(jsonPath("$", hasItem("Soups")));
    }

    // --- Test Create Recipe ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testCreateRecipe_AsUser_Success() throws Exception {
        Recipe newRecipe = new Recipe();
        newRecipe.setTitle("Apple Pie");
        newRecipe.setCategory("Desserts");
        newRecipe.setIngredients(Collections.singletonList("Apples"));
        newRecipe.setInstructions("Bake pie.");
        newRecipe.setCookingTime(60);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRecipe)))
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Apple Pie")))
                .andExpect(jsonPath("$.createdBy", is(userUsername))); // Creator should be the mock user
    }

    @Test
    void testCreateRecipe_Unauthenticated_ReturnsUnauthorized() throws Exception {
        Recipe newRecipe = new Recipe();
        newRecipe.setTitle("Forbidden Pie");
        newRecipe.setCategory("Desserts");

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRecipe)))
                .andExpect(status().isUnauthorized()); // Expect HTTP 401
    }

    // --- Test Get All Recipes ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testGetAllRecipes_AsUser_Success() throws Exception {
        mockMvc.perform(get("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // We created 2 recipes in setUp
                .andExpect(jsonPath("$[0].title", is(testRecipe1.getTitle())))
                .andExpect(jsonPath("$[1].title", is(testRecipe2.getTitle())));
    }

    // --- Test Get Recipe By ID (Global Search) ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testGetRecipeById_Global_Found() throws Exception {
        mockMvc.perform(get("/api/recipes/" + testRecipe1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testRecipe1.getId())))
                .andExpect(jsonPath("$.title", is(testRecipe1.getTitle())));
    }

    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testGetRecipeById_Global_NotFound() throws Exception {
        mockMvc.perform(get("/api/recipes/nonExistentId123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // --- Test Get Recipe By Category and ID ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testGetRecipeByCategoryAndId_Found() throws Exception {
        mockMvc.perform(get("/api/recipes/category/" + testRecipe1.getCategory() + "/id/" + testRecipe1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testRecipe1.getId())))
                .andExpect(jsonPath("$.title", is(testRecipe1.getTitle())))
                .andExpect(jsonPath("$.category", is(testRecipe1.getCategory())));
    }

    // --- Test Update Recipe ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"}) // testRecipe1 was created by userUsername
    void testUpdateRecipe_AsOwner_Success() throws Exception {
        Recipe updatedDetails = new Recipe();
        // ID is not part of request body for update, it's a path variable.
        // The service should preserve the ID.
        updatedDetails.setTitle("Updated Lemon Tart");
        updatedDetails.setCategory(testRecipe1.getCategory()); // Keep same category
        updatedDetails.setIngredients(Arrays.asList("Lemon", "Sugar", "Extra Pastry"));
        updatedDetails.setInstructions("Bake the updated tart.");
        updatedDetails.setCookingTime(50);
        // createdBy should be preserved by the service logic

        mockMvc.perform(put("/api/recipes/" + testRecipe1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Lemon Tart")))
                .andExpect(jsonPath("$.cookingTime", is(50)))
                .andExpect(jsonPath("$.createdBy", is(userUsername))); // Creator should remain the same
    }

    @Test
    @WithMockUser(username = adminUsername, roles = {"ADMIN"}) // Admin can update any recipe
    void testUpdateRecipe_AsAdmin_Success() throws Exception {
        Recipe updatedDetails = new Recipe();
        updatedDetails.setTitle("Admin Updated Lemon Tart");
        updatedDetails.setCategory(testRecipe1.getCategory());
        updatedDetails.setCookingTime(55);

        mockMvc.perform(put("/api/recipes/" + testRecipe1.getId()) // testRecipe1 created by userUsername
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Admin Updated Lemon Tart")))
                .andExpect(jsonPath("$.createdBy", is(userUsername))); // Original creator should be preserved
    }

    @Test
    @WithMockUser(username = "anotherUser", roles = {"USER"}) // Not the owner, not admin
    void testUpdateRecipe_AsNonOwnerNonAdmin_Forbidden() throws Exception {
        Recipe updatedDetails = new Recipe();
        updatedDetails.setTitle("Attempted Update Tart");
        updatedDetails.setCategory(testRecipe1.getCategory());

        mockMvc.perform(put("/api/recipes/" + testRecipe1.getId()) // testRecipe1 created by userUsername
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testUpdateRecipe_NotFound() throws Exception {
        Recipe updatedDetails = new Recipe();
        updatedDetails.setTitle("Non Existent Update");
        updatedDetails.setCategory("Desserts");

        mockMvc.perform(put("/api/recipes/nonExistentId123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }


    // --- Test Delete Recipe ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"}) // testRecipe1 was created by userUsername
    void testDeleteRecipe_AsOwner_Success() throws Exception {
        mockMvc.perform(delete("/api/recipes/" + testRecipe1.getId()))
                .andExpect(status().isNoContent()); // Expect HTTP 204 No Content

        // Verify it's actually deleted (optional, could be a separate test for service)
        mockMvc.perform(get("/api/recipes/" + testRecipe1.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = adminUsername, roles = {"ADMIN"}) // Admin can delete any recipe
    void testDeleteRecipe_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/recipes/" + testRecipe1.getId())) // testRecipe1 created by userUsername
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "anotherUser", roles = {"USER"}) // Not the owner, not admin
    void testDeleteRecipe_AsNonOwnerNonAdmin_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/recipes/" + testRecipe1.getId())) // testRecipe1 created by userUsername
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testDeleteRecipe_NotFound() throws Exception {
        mockMvc.perform(delete("/api/recipes/nonExistentId123"))
                .andExpect(status().isNotFound());
    }

    // --- Test Search Endpoints ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testSearchRecipesByTitle_Found() throws Exception {
        mockMvc.perform(get("/api/recipes/search/title/Lemon")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Lemon Tart")));
    }

    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testSearchRecipesByCategory_Found() throws Exception {
        mockMvc.perform(get("/api/recipes/search/category/Soups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("Soups")));
    }

    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testSearchRecipesByCookingTime_Found() throws Exception {
        mockMvc.perform(get("/api/recipes/search/cookingTime/50") // Lemon Tart (45)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cookingTime", lessThanOrEqualTo(50)));
    }

    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testSearchRecipesByIngredient_Found() throws Exception {
        mockMvc.perform(get("/api/recipes/search/ingredient/Chicken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ingredients", hasItem("Chicken")));
    }

    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testAdvancedSearch_MultipleCriteria_Found() throws Exception {
        // Search for Soups with "Chicken" ingredient and max cooking time 100
        mockMvc.perform(get("/api/recipes/search/advanced")
                        .param("category", "Soups")
                        .param("ingredient", "Chicken")
                        .param("maxCookingTime", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Chicken Soup")));
    }

    // --- Test My Recipes ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testGetMyRecipes_AsUser_Success() throws Exception {
        mockMvc.perform(get("/api/recipes/my-recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // testRecipe1 created by userUsername
                .andExpect(jsonPath("$[0].title", is(testRecipe1.getTitle())));
    }

    @Test
    @WithMockUser(username = adminUsername, roles = {"ADMIN"})
    void testGetMyRecipes_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/recipes/my-recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // testRecipe2 created by adminUsername
                .andExpect(jsonPath("$[0].title", is(testRecipe2.getTitle())));
    }


    // --- Test Admin All Recipes ---
    @Test
    @WithMockUser(username = adminUsername, roles = {"ADMIN"})
    void testAdminGetAllRecipes_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/recipes/admin/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = userUsername, roles = {"USER"}) // User role should be forbidden
    void testAdminGetAllRecipes_AsUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/recipes/admin/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // --- Test Input Validation (Example for Create Recipe) ---
    @Test
    @WithMockUser(username = userUsername, roles = {"USER"})
    void testCreateRecipe_InvalidInput_ReturnsBadRequest() throws Exception {
        Recipe invalidRecipe = new Recipe(); // Missing title, ingredients etc.
        invalidRecipe.setCategory("Desserts");
        // cookingTime might default or be null, let's explicitly make it invalid for the DTO constraint
        invalidRecipe.setCookingTime(0); // Assuming @Min(1) on RecipeDto's cookingTime

        MvcResult result = mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRecipe)))
                .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
                .andExpect(jsonPath("$.errors", notNullValue()))
                .andExpect(jsonPath("$.errors.title", is("Title is required"))) // From @NotBlank on RecipeDto
                .andExpect(jsonPath("$.errors.ingredients", is("At least one ingredient is required"))) // From @NotEmpty
                .andExpect(jsonPath("$.errors.instructions", is("Instructions are required"))) // From @NotBlank
                .andExpect(jsonPath("$.errors.cookingTime", is("Cooking time must be at least 1 minute"))) // From @Min(1)
                .andReturn();

        // System.out.println(result.getResponse().getContentAsString()); // For debugging error response
    }
}