package com.example.recipeoop_1.service;

import com.example.recipeoop_1.model.Recipe;
import java.util.List;

public interface RecipeService {

    // Create a new recipe
    Recipe createRecipe(Recipe recipeDetails, String username);

    // Get all recipes (across all categories)
    List<Recipe> getAllRecipes();

    // Get recipe by ID and category
    Recipe getRecipeById(String category, String id);

    // Get recipe by ID (searching across all categories)
    Recipe getRecipeById(String id);

    // Update recipe
    Recipe updateRecipe(String id, Recipe recipeDetails);

    // Delete recipe
    void deleteRecipe(String id);

    // Get recipes by user
    List<Recipe> getRecipesByUser(String username);

    // Search recipes by title
    List<Recipe> searchRecipesByTitle(String title);

    // Get recipes by category
    List<Recipe> searchRecipesByCategory(String category);

    // Search recipes by cooking time
    List<Recipe> searchRecipesByCookingTime(Integer cookingTime);

    // Search recipes by ingredient
    List<Recipe> searchRecipesByIngredient(String ingredient);

    // Advanced search with multiple criteria
    List<Recipe> advancedSearch(String title, String category, Integer maxCookingTime, String ingredient);
}