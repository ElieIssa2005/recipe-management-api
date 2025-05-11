package com.example.recipeoop_1.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.ArrayList;

/**
 * This is a simplified Recipe model with the createdBy field.
 * Make sure to save this file in the exact path:
 * src/main/java/com/example/recipeoop_1/model/Recipe.java
 */
@Document(collection = "recipes")
@Schema(description = "Recipe information")
public class Recipe {

    @Id
    private String id;

    private String title;

    private List<String> ingredients = new ArrayList<>();

    private String instructions;

    private Integer cookingTime;

    private String category;

    private String createdBy;  // Make sure this field exists

    // Default constructor
    public Recipe() {
    }

    // Getter and setter for createdBy
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // Getters and setters for other fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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