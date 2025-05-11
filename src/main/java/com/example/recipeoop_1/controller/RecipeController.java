package com.example.recipeoop_1.controller;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.service.CategoryService;
import com.example.recipeoop_1.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipe Controller", description = "Recipe Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class RecipeController {

    private final RecipeService recipeService;
    private final CategoryService categoryService;

    @Autowired
    public RecipeController(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
    }

    // Get all available categories
    @Operation(summary = "Get all available recipe categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all categories",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Create a new recipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recipe created successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Recipe savedRecipe = recipeService.createRecipe(recipe, username);
        return new ResponseEntity<>(savedRecipe, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all recipes from all categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all recipes",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        List<Recipe> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Get a recipe by ID (searches all categories)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the recipe",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "404", description = "Recipe not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Recipe> getRecipeById(
            @Parameter(description = "ID of the recipe to be retrieved")
            @PathVariable String id) {
        try {
            Recipe recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(recipe);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get a recipe by category and ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the recipe",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "404", description = "Recipe not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/category/{category}/id/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Recipe> getRecipeByCategoryAndId(
            @Parameter(description = "Category of the recipe")
            @PathVariable String category,
            @Parameter(description = "ID of the recipe to be retrieved")
            @PathVariable String id) {
        try {
            Recipe recipe = recipeService.getRecipeById(category, id);
            if (recipe == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(recipe);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update a recipe (can change category)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipe updated successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "404", description = "Recipe not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Recipe> updateRecipe(
            @Parameter(description = "ID of the recipe to be updated")
            @PathVariable String id,
            @RequestBody Recipe recipeDetails) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get existing recipe
            Recipe existingRecipe = recipeService.getRecipeById(id);

            // Check if user is admin or the creator of the recipe
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !username.equals(existingRecipe.getCreatedBy())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Recipe updatedRecipe = recipeService.updateRecipe(id, recipeDetails);
            return ResponseEntity.ok(updatedRecipe);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a recipe by ID (searches all categories)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recipe deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Recipe not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteRecipe(
            @Parameter(description = "ID of the recipe to be deleted")
            @PathVariable String id) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get existing recipe
            Recipe existingRecipe = recipeService.getRecipeById(id);

            // Check if user is admin or the creator of the recipe
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !username.equals(existingRecipe.getCreatedBy())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            recipeService.deleteRecipe(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a recipe by category and ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recipe deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Recipe not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/category/{category}/id/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteRecipeByCategoryAndId(
            @Parameter(description = "Category of the recipe")
            @PathVariable String category,
            @Parameter(description = "ID of the recipe to be deleted")
            @PathVariable String id) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get existing recipe
            Recipe existingRecipe = recipeService.getRecipeById(category, id);
            if (existingRecipe == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if user is admin or the creator of the recipe
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !username.equals(existingRecipe.getCreatedBy())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Delete from specific category
            recipeService.deleteRecipe(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Search recipes by title")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found recipes matching title",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search/title/{title}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> searchRecipesByTitle(
            @Parameter(description = "Title to search for")
            @PathVariable String title) {
        List<Recipe> recipes = recipeService.searchRecipesByTitle(title);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Search recipes by category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found recipes matching category",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search/category/{category}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> searchRecipesByCategory(
            @Parameter(description = "Category to search for")
            @PathVariable String category) {
        List<Recipe> recipes = recipeService.searchRecipesByCategory(category);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Search recipes by cooking time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found recipes with cooking time less than or equal to specified minutes",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search/cookingTime/{minutes}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> searchRecipesByCookingTime(
            @Parameter(description = "Maximum cooking time in minutes")
            @PathVariable Integer minutes) {
        List<Recipe> recipes = recipeService.searchRecipesByCookingTime(minutes);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Search recipes by ingredient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found recipes containing the specified ingredient",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search/ingredient/{ingredient}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> searchRecipesByIngredient(
            @Parameter(description = "Ingredient to search for")
            @PathVariable String ingredient) {
        List<Recipe> recipes = recipeService.searchRecipesByIngredient(ingredient);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Advanced search with multiple criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found recipes matching the criteria",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search/advanced")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> advancedSearch(
            @Parameter(description = "Title to search for (optional)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Category to filter by (optional)")
            @RequestParam(required = false) String category,
            @Parameter(description = "Maximum cooking time in minutes (optional)")
            @RequestParam(required = false) Integer maxCookingTime,
            @Parameter(description = "Ingredient to search for (optional)")
            @RequestParam(required = false) String ingredient) {

        List<Recipe> recipes = recipeService.advancedSearch(title, category, maxCookingTime, ingredient);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Get my recipes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found user's recipes",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/my-recipes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Recipe>> getMyRecipes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<Recipe> myRecipes = recipeService.getRecipesByUser(username);
        return ResponseEntity.ok(myRecipes);
    }

    // Admin-only endpoints
    @Operation(summary = "Admin endpoint - Get all recipes with user info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all recipes",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Recipe>> adminGetAllRecipes() {
        List<Recipe> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }
}