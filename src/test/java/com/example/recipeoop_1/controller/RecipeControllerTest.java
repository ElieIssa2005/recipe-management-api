package com.example.recipeoop_1.controller;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.repository.RecipeRepository; // Import RecipeRepository
import com.example.recipeoop_1.security.JwtAuthenticationEntryPoint;
import com.example.recipeoop_1.security.JwtTokenUtil;
import com.example.recipeoop_1.security.JwtUserDetailsService;
import com.example.recipeoop_1.service.CategoryService;
import com.example.recipeoop_1.service.RecipeService;
import com.example.recipeoop_1.exception.RecipeNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
// Import security config to be used by @WebMvcTest
import com.example.recipeoop_1.security.WebSecurityConfig;
import com.example.recipeoop_1.config.AppConfig; // Import AppConfig for PasswordEncoder and AuthManager if needed by WebSecurityConfig indirectly

// Import to exclude Mongo auto-configuration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link RecipeController}.
 * These tests focus on the web layer, mocking the service layer dependencies.
 * Uses {@link WebMvcTest} for focused testing of the controller.
 * Excludes MongoDB auto-configuration and mocks RecipeRepository to prevent errors.
 */
@WebMvcTest(RecipeController.class)
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class}) // Exclude Mongo
@Import({WebSecurityConfig.class, AppConfig.class}) // Import necessary security and app configurations
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean // Mock the repository to prevent context loading issues with @EnableMongoRepositories
    private RecipeRepository recipeRepository;


    private Recipe testRecipe1;
    private Recipe testRecipe2;
    private User mockUserDetailsUser;
    private User mockUserDetailsAdmin;


    @BeforeEach
    void setUp() {
        testRecipe1 = new Recipe();
        testRecipe1.setId("recipe1_id");
        testRecipe1.setTitle("Pasta Carbonara");
        testRecipe1.setIngredients(Arrays.asList("Spaghetti", "Eggs", "Pancetta"));
        testRecipe1.setInstructions("Cook it well.");
        testRecipe1.setCookingTime(20);
        testRecipe1.setCategory("Main Course");
        testRecipe1.setCreatedBy("user1");

        testRecipe2 = new Recipe();
        testRecipe2.setId("recipe2_id");
        testRecipe2.setTitle("Chocolate Cake");
        testRecipe2.setIngredients(Arrays.asList("Flour", "Sugar", "Cocoa"));
        testRecipe2.setInstructions("Bake it well.");
        testRecipe2.setCookingTime(60);
        testRecipe2.setCategory("Dessert");
        testRecipe2.setCreatedBy("adminUser");

        mockUserDetailsUser = new User("user1", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        mockUserDetailsAdmin = new User("adminUser", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER")));

    }

    /**
     * Test getting all categories with a USER role. Expects 200 OK.
     */
    @Test
    @WithMockUser(roles = "USER")
    void getAllCategories_asUser_shouldReturnCategories() throws Exception {
        // Arrange
        List<String> categories = Arrays.asList("Main Course", "Dessert", "Appetizer");
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("Main Course")))
                .andExpect(jsonPath("$[1]", is("Dessert")));

        verify(categoryService).getAllCategories();
    }

    /**
     * Test creating a recipe with a USER role. Expects 201 Created.
     */
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void createRecipe_asUser_withValidRecipe_shouldReturnCreated() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetailsUser, null, mockUserDetailsUser.getAuthorities())
        );

        Recipe recipeToCreate = new Recipe();
        recipeToCreate.setTitle("New Salad");
        recipeToCreate.setIngredients(Collections.singletonList("Lettuce"));
        recipeToCreate.setInstructions("Mix it.");
        recipeToCreate.setCookingTime(5);
        recipeToCreate.setCategory("Salad");

        Recipe savedRecipe = new Recipe();
        savedRecipe.setId("recipe3_id");
        savedRecipe.setTitle(recipeToCreate.getTitle());
        savedRecipe.setIngredients(recipeToCreate.getIngredients());
        savedRecipe.setInstructions(recipeToCreate.getInstructions());
        savedRecipe.setCookingTime(recipeToCreate.getCookingTime());
        savedRecipe.setCategory(recipeToCreate.getCategory());
        savedRecipe.setCreatedBy("user1");


        when(recipeService.createRecipe(any(Recipe.class), eq("user1"))).thenReturn(savedRecipe);

        // Act & Assert
        mockMvc.perform(post("/api/recipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipeToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("recipe3_id")))
                .andExpect(jsonPath("$.title", is("New Salad")))
                .andExpect(jsonPath("$.createdBy", is("user1")));

        verify(recipeService).createRecipe(any(Recipe.class), eq("user1"));
        SecurityContextHolder.clearContext();
    }

    /**
     * Test getting all recipes with a USER role.
     */
    @Test
    @WithMockUser(roles = "USER")
    void getAllRecipes_asUser_shouldReturnListOfRecipes() throws Exception {
        // Arrange
        List<Recipe> recipes = Arrays.asList(testRecipe1, testRecipe2);
        when(recipeService.getAllRecipes()).thenReturn(recipes);

        // Act & Assert
        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is(testRecipe1.getTitle())))
                .andExpect(jsonPath("$[1].title", is(testRecipe2.getTitle())));

        verify(recipeService).getAllRecipes();
    }

    /**
     * Test getting a recipe by ID when it exists.
     */
    @Test
    @WithMockUser(roles = "USER")
    void getRecipeById_whenExists_shouldReturnRecipe() throws Exception {
        // Arrange
        when(recipeService.getRecipeById("recipe1_id")).thenReturn(testRecipe1);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/recipe1_id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testRecipe1.getId())))
                .andExpect(jsonPath("$.title", is(testRecipe1.getTitle())));
        verify(recipeService).getRecipeById("recipe1_id");
    }

    /**
     * Test getting a recipe by ID when it does not exist. Expects 404 Not Found.
     */
    @Test
    @WithMockUser(roles = "USER")
    void getRecipeById_whenNotExists_shouldReturnNotFound() throws Exception {
        // Arrange
        when(recipeService.getRecipeById("non_existent_id")).thenThrow(new RecipeNotFoundException("Recipe not found"));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/non_existent_id"))
                .andExpect(status().isNotFound());
        verify(recipeService).getRecipeById("non_existent_id");
    }

    /**
     * Test getting a recipe by category and ID when it exists.
     */
    @Test
    @WithMockUser(roles = "USER")
    void getRecipeByCategoryAndId_whenExists_shouldReturnRecipe() throws Exception {
        // Arrange
        String category = "Main Course";
        String id = "recipe1_id";
        when(recipeService.getRecipeById(eq(category), eq(id))).thenReturn(testRecipe1);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/category/{category}/id/{id}", category, id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.category", is(category)));
        verify(recipeService).getRecipeById(eq(category), eq(id));
    }

    /**
     * Test updating a recipe by its creator. Expects 200 OK.
     */
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void updateRecipe_asCreator_shouldReturnOk() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetailsUser, null, mockUserDetailsUser.getAuthorities())
        );

        String recipeId = testRecipe1.getId();
        Recipe updatedDetails = new Recipe();
        updatedDetails.setTitle("Super Pasta Carbonara");
        updatedDetails.setCategory("Main Course");


        Recipe returnedRecipe = new Recipe();
        returnedRecipe.setId(recipeId);
        returnedRecipe.setTitle(updatedDetails.getTitle());
        returnedRecipe.setCategory(updatedDetails.getCategory());
        returnedRecipe.setCreatedBy("user1");


        when(recipeService.getRecipeById(recipeId)).thenReturn(testRecipe1);
        when(recipeService.updateRecipe(eq(recipeId), any(Recipe.class))).thenReturn(returnedRecipe);

        // Act & Assert
        mockMvc.perform(put("/api/recipes/{id}", recipeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Super Pasta Carbonara")));

        verify(recipeService).getRecipeById(recipeId);
        verify(recipeService).updateRecipe(eq(recipeId), any(Recipe.class));
        SecurityContextHolder.clearContext();
    }

    /**
     * Test updating a recipe by an ADMIN (not the creator). Expects 200 OK.
     */
    @Test
    @WithMockUser(username = "adminUser", roles = "ADMIN")
    void updateRecipe_asAdmin_shouldReturnOk() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetailsAdmin, null, mockUserDetailsAdmin.getAuthorities())
        );

        String recipeId = testRecipe1.getId();
        Recipe updatedDetails = new Recipe();
        updatedDetails.setTitle("Admin Updated Pasta");
        updatedDetails.setCategory("Main Course");

        Recipe returnedRecipe = new Recipe();
        returnedRecipe.setId(recipeId);
        returnedRecipe.setTitle(updatedDetails.getTitle());
        returnedRecipe.setCategory(updatedDetails.getCategory());
        returnedRecipe.setCreatedBy("user1");

        when(recipeService.getRecipeById(recipeId)).thenReturn(testRecipe1);
        when(recipeService.updateRecipe(eq(recipeId), any(Recipe.class))).thenReturn(returnedRecipe);

        // Act & Assert
        mockMvc.perform(put("/api/recipes/{id}", recipeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Admin Updated Pasta")));
        SecurityContextHolder.clearContext();
    }

    /**
     * Test updating a recipe by a non-creator, non-admin user. Expects 403 Forbidden.
     */
    @Test
    @WithMockUser(username = "anotherUser", roles = "USER")
    void updateRecipe_asNonCreatorNonAdmin_shouldReturnForbidden() throws Exception {
        // Arrange
        User anotherUserDetails = new User("anotherUser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(anotherUserDetails, null, anotherUserDetails.getAuthorities())
        );

        String recipeId = testRecipe1.getId();
        Recipe updatedDetails = new Recipe();
        updatedDetails.setTitle("Illegal Update");

        when(recipeService.getRecipeById(recipeId)).thenReturn(testRecipe1);

        // Act & Assert
        mockMvc.perform(put("/api/recipes/{id}", recipeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isForbidden());

        verify(recipeService).getRecipeById(recipeId);
        verify(recipeService, never()).updateRecipe(anyString(), any(Recipe.class));
        SecurityContextHolder.clearContext();
    }

    /**
     * Test updating a non-existent recipe. Expects 404 Not Found.
     */
    @Test
    @WithMockUser(roles = "USER")
    void updateRecipe_nonExistent_shouldReturnNotFound() throws Exception {
        String recipeId = "nonExistentId";
        Recipe recipeDetails = new Recipe();
        recipeDetails.setTitle("Title");

        when(recipeService.getRecipeById(recipeId)).thenThrow(new RecipeNotFoundException("Not found"));

        mockMvc.perform(put("/api/recipes/{id}", recipeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipeDetails)))
                .andExpect(status().isNotFound());
    }


    /**
     * Test deleting a recipe by its creator. Expects 204 No Content.
     */
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void deleteRecipe_asCreator_shouldReturnNoContent() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetailsUser, null, mockUserDetailsUser.getAuthorities())
        );
        String recipeId = testRecipe1.getId();

        when(recipeService.getRecipeById(recipeId)).thenReturn(testRecipe1);
        doNothing().when(recipeService).deleteRecipe(recipeId);

        // Act & Assert
        mockMvc.perform(delete("/api/recipes/{id}", recipeId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(recipeService).getRecipeById(recipeId);
        verify(recipeService).deleteRecipe(recipeId);
        SecurityContextHolder.clearContext();
    }

    /**
     * Test deleting a recipe by an ADMIN. Expects 204 No Content.
     */
    @Test
    @WithMockUser(username = "adminUser", roles = "ADMIN")
    void deleteRecipe_asAdmin_shouldReturnNoContent() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetailsAdmin, null, mockUserDetailsAdmin.getAuthorities())
        );
        String recipeId = testRecipe1.getId();

        when(recipeService.getRecipeById(recipeId)).thenReturn(testRecipe1);
        doNothing().when(recipeService).deleteRecipe(recipeId);

        mockMvc.perform(delete("/api/recipes/{id}", recipeId).with(csrf()))
                .andExpect(status().isNoContent());
        SecurityContextHolder.clearContext();
    }

    /**
     * Test deleting a non-existent recipe. Expects 404 Not Found.
     */
    @Test
    @WithMockUser(roles = "ADMIN") // Admin to bypass ownership check for non-existence
    void deleteRecipe_nonExistent_shouldReturnNotFound() throws Exception {
        String recipeId = "nonExistentId";
        when(recipeService.getRecipeById(recipeId)).thenThrow(new RecipeNotFoundException("Not found"));

        mockMvc.perform(delete("/api/recipes/{id}", recipeId).with(csrf()))
                .andExpect(status().isNotFound());
    }


    /**
     * Test searching recipes by title.
     */
    @Test
    @WithMockUser(roles = "USER")
    void searchRecipesByTitle_shouldReturnMatchingRecipes() throws Exception {
        String titleKeyword = "Pasta";
        when(recipeService.searchRecipesByTitle(titleKeyword)).thenReturn(Collections.singletonList(testRecipe1));

        mockMvc.perform(get("/api/recipes/search/title/{title}", titleKeyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(testRecipe1.getTitle())));
        verify(recipeService).searchRecipesByTitle(titleKeyword);
    }

    /**
     * Test searching recipes by category.
     */
    @Test
    @WithMockUser(roles = "USER")
    void searchRecipesByCategory_shouldReturnMatchingRecipes() throws Exception {
        String category = "Dessert";
        when(recipeService.searchRecipesByCategory(category)).thenReturn(Collections.singletonList(testRecipe2));

        mockMvc.perform(get("/api/recipes/search/category/{category}", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is(category)));
        verify(recipeService).searchRecipesByCategory(category);
    }

    /**
     * Test searching recipes by cooking time.
     */
    @Test
    @WithMockUser(roles = "USER")
    void searchRecipesByCookingTime_shouldReturnMatchingRecipes() throws Exception {
        Integer minutes = 20;
        when(recipeService.searchRecipesByCookingTime(minutes)).thenReturn(Collections.singletonList(testRecipe1));

        mockMvc.perform(get("/api/recipes/search/cookingTime/{minutes}", minutes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cookingTime", is(minutes)));
        verify(recipeService).searchRecipesByCookingTime(minutes);
    }

    /**
     * Test searching recipes by ingredient.
     */
    @Test
    @WithMockUser(roles = "USER")
    void searchRecipesByIngredient_shouldReturnMatchingRecipes() throws Exception {
        String ingredient = "Eggs";
        when(recipeService.searchRecipesByIngredient(ingredient)).thenReturn(Arrays.asList(testRecipe1));

        mockMvc.perform(get("/api/recipes/search/ingredient/{ingredient}", ingredient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ingredients[1]", is(ingredient)));
        verify(recipeService).searchRecipesByIngredient(ingredient);
    }

    /**
     * Test advanced search endpoint.
     */
    @Test
    @WithMockUser(roles = "USER")
    void advancedSearch_shouldReturnMatchingRecipes() throws Exception {
        String title = "Cake";
        String category = "Dessert";
        Integer maxCookingTime = 60;
        String ingredient = "Flour";

        when(recipeService.advancedSearch(title, category, maxCookingTime, ingredient))
                .thenReturn(Collections.singletonList(testRecipe2));

        mockMvc.perform(get("/api/recipes/search/advanced")
                        .param("title", title)
                        .param("category", category)
                        .param("maxCookingTime", maxCookingTime.toString())
                        .param("ingredient", ingredient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(testRecipe2.getTitle())));
        verify(recipeService).advancedSearch(title, category, maxCookingTime, ingredient);
    }


    /**
     * Test getting "my-recipes" for an authenticated user.
     */
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getMyRecipes_asAuthenticatedUser_shouldReturnUserRecipes() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetailsUser, null, mockUserDetailsUser.getAuthorities())
        );
        when(recipeService.getRecipesByUser("user1")).thenReturn(Collections.singletonList(testRecipe1));

        mockMvc.perform(get("/api/recipes/my-recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].createdBy", is("user1")));
        verify(recipeService).getRecipesByUser("user1");
        SecurityContextHolder.clearContext();
    }

    /**
     * Test admin getting all recipes. Expects 200 OK.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminGetAllRecipes_asAdmin_shouldReturnAllRecipes() throws Exception {
        List<Recipe> allRecipes = Arrays.asList(testRecipe1, testRecipe2);
        when(recipeService.getAllRecipes()).thenReturn(allRecipes);

        mockMvc.perform(get("/api/recipes/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        verify(recipeService).getAllRecipes();
    }

    /**
     * Test admin getting all recipes by a non-admin user. Expects 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "USER")
    void adminGetAllRecipes_asUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/recipes/admin/all"))
                .andExpect(status().isForbidden());
        verify(recipeService, never()).getAllRecipes();
    }

    /**
     * Test accessing a protected endpoint without authentication. Expects 401 Unauthorized.
     */
    @Test
    @WithAnonymousUser
    void getRecipeById_asAnonymous_shouldReturnUnauthorized() throws Exception {
        // Mock the behavior of the AuthenticationEntryPoint to actually send 401
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized from mock");
            return null;
        }).when(jwtAuthenticationEntryPoint).commence(any(HttpServletRequest.class), any(HttpServletResponse.class), any(AuthenticationException.class));

        mockMvc.perform(get("/api/recipes/recipe1_id"))
                .andExpect(status().isUnauthorized());

        verify(recipeService, never()).getRecipeById(anyString());
    }
}