package com.example.recipeoop_1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * Data Transfer Object (DTO) for representing recipe data, typically used for
 * creating or updating recipes through API requests.
 * <p>
 * This class includes validation constraints for its fields to ensure data integrity
 * when receiving recipe information from clients.
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.2
 * @since 2025-05-14
 */
public class RecipeDto {

    /**
     * The title of the recipe.
     * <p>
     * Constraints:
     * <ul>
     * <li>{@link jakarta.validation.constraints.NotBlank}: Must not be null and must contain at least one non-whitespace character.
     * Message: "Title is required".
     * </ul>
     * Example: "Chocolate Chip Cookies"
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * A list of ingredients for the recipe. Each ingredient is a string.
     * <p>
     * Constraints:
     * <ul>
     * <li>{@link jakarta.validation.constraints.NotEmpty}: The list must not be null and must contain at least one ingredient.
     * Message: "At least one ingredient is required".
     * </ul>
     * Example: {@code ["200g flour", "100g sugar", "100g chocolate chips"]}
     */
    @NotEmpty(message = "At least one ingredient is required")
    private List<String> ingredients;

    /**
     * The instructions for preparing the recipe.
     * <p>
     * Constraints:
     * <ul>
     * <li>{@link jakarta.validation.constraints.NotBlank}: Must not be null and must contain at least one non-whitespace character.
     * Message: "Instructions are required".
     * </ul>
     * Example: "Mix ingredients, bake at 180°C for 15 minutes"
     */
    @NotBlank(message = "Instructions are required")
    private String instructions;

    /**
     * The cooking time for the recipe, in minutes.
     * <p>
     * Constraints:
     * <ul>
     * <li>{@link jakarta.validation.constraints.Min}: Must be at least 1 minute.
     * Message: "Cooking time must be at least 1 minute".
     * </ul>
     * Example: 30
     */
    @Min(value = 1, message = "Cooking time must be at least 1 minute")
    private Integer cookingTime;

    /**
     * The category of the recipe (e.g., Dessert, Main Course).
     * This field is optional.
     * <p>
     * Example: "Dessert"
     */
    private String category;

    // Getters and Setters

    /**
     * Gets the title of the recipe.
     * @return The recipe title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the recipe.
     * @param title The recipe title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the list of ingredients for the recipe.
     * @return A list of ingredient strings.
     */
    public List<String> getIngredients() {
        return ingredients;
    }

    /**
     * Sets the list of ingredients for the recipe.
     * @param ingredients A list of ingredient strings to set.
     */
    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Gets the instructions for preparing the recipe.
     * @return The recipe instructions.
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Sets the instructions for preparing the recipe.
     * @param instructions The recipe instructions to set.
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Gets the cooking time for the recipe in minutes.
     * @return The cooking time in minutes.
     */
    public Integer getCookingTime() {
        return cookingTime;
    }

    /**
     * Sets the cooking time for the recipe in minutes.
     * @param cookingTime The cooking time in minutes to set.
     */
    public void setCookingTime(Integer cookingTime) {
        this.cookingTime = cookingTime;
    }

    /**
     * Gets the category of the recipe.
     * @return The recipe category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the recipe.
     * @param category The recipe category to set.
     */
    public void setCategory(String category) {
        this.category = category;
    }
}