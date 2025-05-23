package com.example.recipeoop_1.service;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.exception.RecipeNotFoundException; // Assuming this exception exists for getRecipeById

import java.util.List;

/**
 * Service interface defining operations for managing recipes.
 * <p>
 * This interface outlines the contract for creating, retrieving, updating, deleting,
 * and searching for recipes. Implementations of this service will handle the
 * business logic associated with recipe management, potentially interacting with
 * data repositories and other services like {@link CategoryService}.
 * </p>
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.0
 * @since 2025-05-14
 * @see Recipe
 * @see RecipeServiceImpl
 */
public interface RecipeService {

    /**
     * Creates a new recipe and associates it with the given username.
     * <p>
     * The implementation should handle persisting the recipe details and linking
     * it to the user who created it. It may also involve ensuring the specified
     * category exists or is created.
     * </p>
     *
     * @param recipeDetails The {@link Recipe} object containing the details of the recipe to be created.
     * Must not be {@code null}.
     * @param username The username of the user creating the recipe. Must not be {@code null} or empty.
     * @return The created and persisted {@link Recipe} object, typically including its generated ID.
     */
    // Original comment: Create a new recipe
    Recipe createRecipe(Recipe recipeDetails, String username);

    /**
     * Retrieves a list of all recipes available in the system, across all categories.
     *
     * @return A {@link List} of all {@link Recipe} objects. Returns an empty list if no recipes exist.
     */
    // Original comment: Get all recipes (across all categories)
    List<Recipe> getAllRecipes();

    /**
     * Retrieves a specific recipe by its category and unique identifier (ID).
     *
     * @param category The category name in which to search for the recipe. Must not be {@code null} or empty.
     * @param id The unique ID of the recipe to retrieve. Must not be {@code null} or empty.
     * @return The found {@link Recipe} object, or {@code null} if no recipe matches the given ID and category.
     * Alternatively, implementations might throw {@link RecipeNotFoundException}.
     * @throws RecipeNotFoundException if the recipe is not found (depending on implementation).
     */
    // Original comment: Get recipe by ID and category
    Recipe getRecipeById(String category, String id);

    /**
     * Retrieves a specific recipe by its unique identifier (ID), searching across all categories.
     *
     * @param id The unique ID of the recipe to retrieve. Must not be {@code null} or empty.
     * @return The found {@link Recipe} object.
     * @throws RecipeNotFoundException if no recipe with the given ID exists in any category.
     */
    // Original comment: Get recipe by ID (searching across all categories)
    Recipe getRecipeById(String id) throws RecipeNotFoundException;

    /**
     * Updates an existing recipe identified by its ID with new details.
     * <p>
     * The implementation should find the existing recipe, apply the changes from
     * {@code recipeDetails}, and persist the updated recipe.
     * It may also need to handle changes in category, potentially moving the recipe
     * between underlying storage structures.
     * </p>
     *
     * @param id The unique ID of the recipe to update. Must not be {@code null} or empty.
     * @param recipeDetails A {@link Recipe} object containing the new details for the recipe.
     * Must not be {@code null}.
     * @return The updated {@link Recipe} object.
     * @throws RecipeNotFoundException if no recipe with the given ID exists.
     */
    // Original comment: Update recipe
    Recipe updateRecipe(String id, Recipe recipeDetails) throws RecipeNotFoundException;

    /**
     * Deletes a recipe from the system based on its unique identifier (ID).
     * <p>
     * The implementation should ensure the recipe is removed from persistence.
     * </p>
     *
     * @param id The unique ID of the recipe to delete. Must not be {@code null} or empty.
     * @throws RecipeNotFoundException if no recipe with the given ID exists.
     */
    // Original comment: Delete recipe
    void deleteRecipe(String id) throws RecipeNotFoundException;

    /**
     * Retrieves all recipes created by a specific user.
     *
     * @param username The username of the user whose recipes are to be retrieved.
     * Must not be {@code null} or empty.
     * @return A {@link List} of {@link Recipe} objects created by the specified user.
     * Returns an empty list if the user has not created any recipes or if the user does not exist.
     */
    // Original comment: Get recipes by user
    List<Recipe> getRecipesByUser(String username);

    /**
     * Searches for recipes whose titles contain the given keyword.
     * The search should ideally be case-insensitive.
     *
     * @param title The keyword to search for within recipe titles.
     * @return A {@link List} of {@link Recipe} objects whose titles match the keyword.
     * Returns an empty list if no matches are found.
     */
    // Original comment: Search recipes by title
    List<Recipe> searchRecipesByTitle(String title);

    /**
     * Retrieves all recipes belonging to a specific category.
     *
     * @param category The category name to filter recipes by. Must not be {@code null} or empty.
     * @return A {@link List} of {@link Recipe} objects within the specified category.
     * Returns an empty list if the category does not exist or contains no recipes.
     */
    // Original comment: Get recipes by category
    List<Recipe> searchRecipesByCategory(String category);

    /**
     * Searches for recipes with a cooking time less than or equal to the specified number of minutes.
     *
     * @param cookingTime The maximum cooking time in minutes. Must be a non-negative integer.
     * @return A {@link List} of {@link Recipe} objects that meet the cooking time criteria.
     * Returns an empty list if no such recipes are found.
     */
    // Original comment: Search recipes by cooking time
    List<Recipe> searchRecipesByCookingTime(Integer cookingTime);

    /**
     * Searches for recipes that contain a specific ingredient.
     * The search should ideally be case-insensitive and match partial ingredient names.
     *
     * @param ingredient The ingredient keyword to search for within the recipe's ingredients list.
     * @return A {@link List} of {@link Recipe} objects that contain the specified ingredient.
     * Returns an empty list if no matches are found.
     */
    // Original comment: Search recipes by ingredient
    List<Recipe> searchRecipesByIngredient(String ingredient);

    /**
     * Performs an advanced search for recipes based on multiple optional criteria.
     * <p>
     * Allows searching by any combination of title, category, maximum cooking time, and ingredient.
     * If a criterion is {@code null} or empty (for strings), it should be ignored in the search.
     * </p>
     *
     * @param title Optional: A keyword to search for in recipe titles.
     * @param category Optional: The category name to filter by.
     * @param maxCookingTime Optional: The maximum cooking time in minutes.
     * @param ingredient Optional: An ingredient keyword to search for.
     * @return A {@link List} of {@link Recipe} objects that match all provided criteria.
     * Returns an empty list if no recipes match the combined criteria.
     */
    // Original comment: Advanced search with multiple criteria
    List<Recipe> advancedSearch(String title, String category, Integer maxCookingTime, String ingredient);
}