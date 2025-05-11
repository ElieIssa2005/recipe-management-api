package com.example.recipeoop_1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;

import java.util.List;

public class RecipeDto {

    @Schema(description = "Recipe title", example = "Chocolate Chip Cookies")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "List of ingredients", example = "[\"200g flour\", \"100g sugar\", \"100g chocolate chips\"]")
    @NotEmpty(message = "At least one ingredient is required")
    private List<String> ingredients;

    @Schema(description = "Recipe instructions", example = "Mix ingredients, bake at 180°C for 15 minutes")
    @NotBlank(message = "Instructions are required")
    private String instructions;

    @Schema(description = "Cooking time in minutes", example = "30")
    @Min(value = 1, message = "Cooking time must be at least 1 minute")
    private Integer cookingTime;

    @Schema(description = "Recipe category", example = "Dessert")
    private String category;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(Integer cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}