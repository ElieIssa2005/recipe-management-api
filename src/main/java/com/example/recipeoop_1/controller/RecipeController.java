package com.example.recipeoop_1.controller;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.service.CategoryService;
import com.example.recipeoop_1.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests related to recipe management.
 * <p>
 * Provides RESTful endpoints for creating, reading, updating, deleting (CRUD),
 * and searching recipes. It also includes an endpoint to list all available recipe categories.
 * Authentication is required for all endpoints, and specific roles (USER, ADMIN)
 * are enforced per endpoint as detailed in method documentation.
 * The base path for these endpoints is {@code /api/recipes}.
 * </p>
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.1
 * @since 2025-05-14
 */
@RestController
@RequestMapping("/api/recipes")
// @Tag(name = "Recipe Controller", description = "Recipe Management APIs") // Swagger annotation removed
// @SecurityRequirement(name = "bearerAuth") // Swagger annotation removed
public class RecipeController {

    private final RecipeService recipeService;
    private final CategoryService categoryService;

    /**
     * Constructs a {@code RecipeController} with the necessary service dependencies.
     *
     * @param recipeService Service for recipe-related operations (e.g., {@link RecipeService}).
     * @param categoryService Service for category-related operations (e.g., {@link CategoryService}).
     */
    @Autowired
    public RecipeController(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
    }

    /**
     * Retrieves a list of all available recipe categories.
     * <p>
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/categories
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Successfully retrieved the list of categories. Returns a list of strings.
     * <pre>{@code
     * ["Dessert", "Main Course", "Appetizer"]
     * }</pre>
     * </li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @return A {@link ResponseEntity} containing a list of category names and HTTP status 200 (OK).
     */
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Creates a new recipe in the database.
     * <p>
     * This endpoint expects a JSON representation of the recipe in the request body.
     * The username of the authenticated user making the request will be associated as the creator of the recipe.
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: POST
     * Path: /api/recipes
     * <p>
     * Request Body (Example JSON):
     * <pre>{@code
     * {
     * "title": "Spaghetti Carbonara",
     * "ingredients": ["Spaghetti", "Eggs", "Pancetta", "Pecorino Romano", "Black Pepper"],
     * "instructions": "Cook spaghetti. Fry pancetta. Mix eggs and cheese. Combine all.",
     * "cookingTime": 20,
     * "category": "Main Course"
     * }
     * }</pre>
     * (See {@link Recipe} for details on the request/response structure)
     * <p>
     * Response:
     * <ul>
     * <li>201 CREATED: If the recipe is created successfully. Returns the created {@link Recipe} object in the response body.</li>
     * <li>400 BAD REQUEST: If the input data is invalid (e.g., missing required fields, validation errors).</li>
     * <li>401 UNAUTHORIZED: If the user is not authenticated.</li>
     * <li>403 FORBIDDEN: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @param recipe The {@link Recipe} object to be created, populated from the request body.
     * Must not be null. Title, ingredients, instructions, and cooking time are typically required.
     * @return A {@link ResponseEntity} containing the created {@link Recipe} and HTTP status 201 (Created),
     * or an appropriate error status if creation fails.
     * @see RecipeService#createRecipe(Recipe, String)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Recipe savedRecipe = recipeService.createRecipe(recipe, username);
        return new ResponseEntity<>(savedRecipe, HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of all recipes from all categories.
     * <p>
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Successfully retrieved all recipes. Returns a list of {@link Recipe} objects.
     * <pre>{@code
     * [
     * { "id": "1", "title": "Recipe A", ... },
     * { "id": "2", "title": "Recipe B", ... }
     * ]
     * }</pre>
     * </li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @return A {@link ResponseEntity} containing a list of all {@link Recipe} objects and HTTP status 200 (OK).
     * @see RecipeService#getAllRecipes()
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        List<Recipe> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    /**
     * Retrieves a specific recipe by its ID.
     * <p>
     * This method searches across all categories for the recipe with the given ID.
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/{id}
     * <p>
     * Path Parameters:
     * <ul><li>{@code id} (String): The unique identifier of the recipe to retrieve.</li></ul>
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: If the recipe is found. Returns the {@link Recipe} object in the response body.</li>
     * <li>404 NOT FOUND: If no recipe with the given ID exists across any category.</li>
     * <li>401 UNAUTHORIZED: If the user is not authenticated.</li>
     * <li>403 FORBIDDEN: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @param id The ID of the recipe to retrieve. Must not be null or empty.
     * @return A {@link ResponseEntity} containing the found {@link Recipe} and HTTP status 200 (OK),
     * or HTTP status 404 (Not Found) if the recipe doesn't exist.
     * @see RecipeService#getRecipeById(String)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable String id) {
        try {
            Recipe recipe = recipeService.getRecipeById(id); // This service method should throw an exception if not found
            return ResponseEntity.ok(recipe);
        } catch (RuntimeException e) { // Catch a more specific exception like RecipeNotFoundException if defined
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves a specific recipe by its category and ID.
     * <p>
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/category/{category}/id/{id}
     * <p>
     * Path Parameters:
     * <ul>
     * <li>{@code category} (String): The category of the recipe.
     * <li>{@code id} (String): The unique identifier of the recipe within that category.
     * </ul>
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: If the recipe is found. Returns the {@link Recipe} object.</li>
     * <li>404 NOT FOUND: If no recipe with the given ID exists in the specified category.</li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @param category The category name of the recipe.
     * @param id The ID of the recipe to retrieve.
     * @return A {@link ResponseEntity} containing the found {@link Recipe} and HTTP status 200 (OK),
     * or HTTP status 404 (Not Found).
     * @see RecipeService#getRecipeById(String, String)
     */
    @GetMapping("/category/{category}/id/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Recipe> getRecipeByCategoryAndId(
            @PathVariable String category,
            @PathVariable String id) {
        try {
            Recipe recipe = recipeService.getRecipeById(category, id);
            if (recipe == null) { // Service method might return null or throw exception
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(recipe);
        } catch (RuntimeException e) { // Catch specific exception if service throws one
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates an existing recipe by its ID.
     * <p>
     * The recipe details are provided in the request body.
     * A user can update a recipe if they are an ADMIN or if they are the original creator of the recipe.
     * If the category of the recipe is changed, the recipe might be moved to a different underlying storage/collection.
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: PUT
     * Path: /api/recipes/{id}
     * <p>
     * Path Parameters:
     * <ul><li>{@code id} (String): The ID of the recipe to update.</li></ul>
     * <p>
     * Request Body (Example JSON):
     * <pre>{@code
     * {
     * "title": "Updated Spaghetti Carbonara",
     * "ingredients": ["Spaghetti", "Eggs", "Guanciale", "Pecorino Romano", "Black Pepper"],
     * "instructions": "Updated instructions...",
     * "cookingTime": 25,
     * "category": "Main Course"
     * }
     * }</pre>
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: If the recipe is updated successfully. Returns the updated {@link Recipe} object.</li>
     * <li>400 BAD REQUEST: If the input data is invalid.</li>
     * <li>401 UNAUTHORIZED: If the user is not authenticated.</li>
     * <li>403 FORBIDDEN: If the authenticated user is not the creator and not an ADMIN.</li>
     * <li>404 NOT FOUND: If no recipe with the given ID exists.</li>
     * </ul>
     *
     * @param id The ID of the recipe to be updated.
     * @param recipeDetails A {@link Recipe} object containing the new details for the recipe.
     * @return A {@link ResponseEntity} containing the updated {@link Recipe} and HTTP status 200 (OK),
     * or an appropriate error status.
     * @see RecipeService#updateRecipe(String, Recipe)
     * @see RecipeService#getRecipeById(String)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable String id,
            @RequestBody Recipe recipeDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Recipe existingRecipe = recipeService.getRecipeById(id); // Throws if not found

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !username.equals(existingRecipe.getCreatedBy())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDetails);
            return ResponseEntity.ok(updatedRecipe);
        } catch (RuntimeException e) { // Catch specific RecipeNotFoundException
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a specific recipe by its ID.
     * <p>
     * This method searches across all categories for the recipe to delete.
     * A user can delete a recipe if they are an ADMIN or if they are the original creator of the recipe.
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: DELETE
     * Path: /api/recipes/{id}
     * <p>
     * Path Parameters:
     * <ul><li>{@code id} (String): The ID of the recipe to delete.</li></ul>
     * <p>
     * Response:
     * <ul>
     * <li>204 NO CONTENT: If the recipe is deleted successfully.</li>
     * <li>401 UNAUTHORIZED: If the user is not authenticated.</li>
     * <li>403 FORBIDDEN: If the authenticated user is not the creator and not an ADMIN.</li>
     * <li>404 NOT FOUND: If no recipe with the given ID exists.</li>
     * </ul>
     *
     * @param id The ID of the recipe to be deleted.
     * @return A {@link ResponseEntity} with HTTP status 204 (No Content) on successful deletion,
     * or an appropriate error status.
     * @see RecipeService#deleteRecipe(String)
     * @see RecipeService#getRecipeById(String)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable String id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Recipe existingRecipe = recipeService.getRecipeById(id); // Throws if not found

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !username.equals(existingRecipe.getCreatedBy())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            recipeService.deleteRecipe(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) { // Catch specific RecipeNotFoundException
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a specific recipe by its category and ID.
     * <p>
     * A user can delete a recipe if they are an ADMIN or if they are the original creator of the recipe.
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: DELETE
     * Path: /api/recipes/category/{category}/id/{id}
     * <p>
     * Path Parameters:
     * <ul>
     * <li>{@code category} (String): The category of the recipe.
     * <li>{@code id} (String): The ID of the recipe to delete from the specified category.
     * </ul>
     * <p>
     * Response:
     * <ul>
     * <li>204 NO CONTENT: If the recipe is deleted successfully.</li>
     * <li>401 UNAUTHORIZED: If the user is not authenticated.</li>
     * <li>403 FORBIDDEN: If the authenticated user is not the creator and not an ADMIN.</li>
     * <li>404 NOT FOUND: If no recipe with the given ID exists in the specified category.</li>
     * </ul>
     *
     * @param category The category of the recipe to delete.
     * @param id The ID of the recipe to delete.
     * @return A {@link ResponseEntity} with HTTP status 204 (No Content) on successful deletion,
     * or an appropriate error status.
     * @see RecipeService#deleteRecipe(String)
     * @see RecipeService#getRecipeById(String, String)
     */
    @DeleteMapping("/category/{category}/id/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteRecipeByCategoryAndId(
            @PathVariable String category,
            @PathVariable String id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Recipe existingRecipe = recipeService.getRecipeById(category, id); // Service method might return null

            if (existingRecipe == null) {
                return ResponseEntity.notFound().build();
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !username.equals(existingRecipe.getCreatedBy())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            recipeService.deleteRecipe(id); // Assumes deleteRecipe by ID is sufficient even if category is known
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) { // Catch specific RecipeNotFoundException if service throws it
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Searches for recipes by a keyword in their title.
     * <p>
     * The search is case-insensitive. Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/search/title/{title}
     * <p>
     * Path Parameters:
     * <ul><li>{@code title} (String): The keyword to search for in recipe titles.</li></ul>
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Returns a list of matching {@link Recipe} objects. The list may be empty if no matches are found.</li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @param title The title keyword to search for.
     * @return A {@link ResponseEntity} containing a list of matching {@link Recipe} objects.
     * @see RecipeService#searchRecipesByTitle(String)
     */
    @GetMapping("/search/title/{title}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> searchRecipesByTitle(
            @PathVariable String title) {
        List<Recipe> recipes = recipeService.searchRecipesByTitle(title);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Searches for recipes by their category name.
     * <p>
     * The search is typically case-insensitive depending on the service implementation.
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/search/category/{category}
     * <p>
     * Path Parameters:
     * <ul><li>{@code category} (String): The category name to search for.</li></ul>
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Returns a list of {@link Recipe} objects belonging to the specified category.
     * The list may be empty.</li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @param category The category name to search for.
     * @return A {@link ResponseEntity} containing a list of matching {@link Recipe} objects.
     * @see RecipeService#searchRecipesByCategory(String)
     */
    @GetMapping("/search/category/{category}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> searchRecipesByCategory(
            @PathVariable String category) {
        List<Recipe> recipes = recipeService.searchRecipesByCategory(category);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Searches for recipes with a cooking time less than or equal to the specified number of minutes.
     * <p>
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/search/cookingTime/{minutes}
     * <p>
     * Path Parameters:
     * <ul><li>{@code minutes} (Integer): The maximum cooking time in minutes.</li></ul>
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Returns a list of {@link Recipe} objects matching the criteria. The list may be empty.</li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @param minutes The maximum cooking time in minutes.
     * @return A {@link ResponseEntity} containing a list of matching {@link Recipe} objects.
     * @see RecipeService#searchRecipesByCookingTime(Integer)
     */
    @GetMapping("/search/cookingTime/{minutes}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> searchRecipesByCookingTime(
            @PathVariable Integer minutes) {
        List<Recipe> recipes = recipeService.searchRecipesByCookingTime(minutes);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Searches for recipes containing a specific ingredient.
     * <p>
     * The search is typically case-insensitive. Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/search/ingredient/{ingredient}
     * <p>
     * Path Parameters:
     * <ul><li>{@code ingredient} (String): The ingredient name or keyword to search for.</li></ul>
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Returns a list of {@link Recipe} objects containing the specified ingredient.
     * The list may be empty.</li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @param ingredient The ingredient to search for.
     * @return A {@link ResponseEntity} containing a list of matching {@link Recipe} objects.
     * @see RecipeService#searchRecipesByIngredient(String)
     */
    @GetMapping("/search/ingredient/{ingredient}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> searchRecipesByIngredient(
            @PathVariable String ingredient) {
        List<Recipe> recipes = recipeService.searchRecipesByIngredient(ingredient);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Performs an advanced search for recipes based on multiple optional criteria.
     * <p>
     * Criteria include title, category, maximum cooking time, and an ingredient.
     * All parameters are optional. If a parameter is not provided, it's not included in the search filter.
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/search/advanced
     * <p>
     * Query Parameters (all optional):
     * <ul>
     * <li>{@code title} (String): Keyword to search in recipe titles.
     * <li>{@code category} (String): Category name to filter by.
     * <li>{@code maxCookingTime} (Integer): Maximum cooking time in minutes.
     * <li>{@code ingredient} (String): Ingredient name or keyword to search for.
     * </ul>
     * Example: {@code /api/recipes/search/advanced?title=chicken&category=Main Course&maxCookingTime=60}
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Returns a list of {@link Recipe} objects matching all provided criteria.
     * The list may be empty.</li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role.</li>
     * </ul>
     *
     * @param title Optional title keyword.
     * @param category Optional category name.
     * @param maxCookingTime Optional maximum cooking time in minutes.
     * @param ingredient Optional ingredient keyword.
     * @return A {@link ResponseEntity} containing a list of matching {@link Recipe} objects.
     * @see RecipeService#advancedSearch(String, String, Integer, String)
     */
    @GetMapping("/search/advanced")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer maxCookingTime,
            @RequestParam(required = false) String ingredient) {

        List<Recipe> recipes = recipeService.advancedSearch(title, category, maxCookingTime, ingredient);
        return ResponseEntity.ok(recipes);
    }

    /**
     * Retrieves all recipes created by the currently authenticated user.
     * <p>
     * Requires USER or ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/my-recipes
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Returns a list of {@link Recipe} objects created by the user. The list may be empty.</li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the required role (though this is unlikely
     * as both USER and ADMIN can access their own recipes).</li>
     * </ul>
     *
     * @return A {@link ResponseEntity} containing a list of the authenticated user's {@link Recipe} objects.
     * @see RecipeService#getRecipesByUser(String)
     */
    @GetMapping("/my-recipes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> getMyRecipes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<Recipe> myRecipes = recipeService.getRecipesByUser(username);
        return ResponseEntity.ok(myRecipes);
    }

    /**
     * Retrieves all recipes in the system. This is an admin-only endpoint.
     * <p>
     * This endpoint provides a comprehensive list of all recipes, potentially for administrative purposes.
     * It differs from {@link #getAllRecipes()} in its authorization requirement (ADMIN role only).
     * Requires ADMIN role.
     * </p>
     * HTTP Method: GET
     * Path: /api/recipes/admin/all
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Successfully retrieved all recipes. Returns a list of {@link Recipe} objects.</li>
     * <li>401 Unauthorized: If the user is not authenticated.</li>
     * <li>403 Forbidden: If the authenticated user does not have the ADMIN role.</li>
     * </ul>
     *
     * @return A {@link ResponseEntity} containing a list of all {@link Recipe} objects in the system.
     * @see RecipeService#getAllRecipes()
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Recipe>> adminGetAllRecipes() {
        List<Recipe> recipes = recipeService.getAllRecipes(); // Same service method as public getAllRecipes
        return ResponseEntity.ok(recipes);
    }
}