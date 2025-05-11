package com.example.recipeoop_1.service;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeServiceImpl implements RecipeService {

    private final MongoTemplate mongoTemplate;
    private final CategoryService categoryService;

    @Autowired
    public RecipeServiceImpl(MongoTemplate mongoTemplate, CategoryService categoryService) {
        this.mongoTemplate = mongoTemplate;
        this.categoryService = categoryService;
    }

    @Override
    public Recipe createRecipe(Recipe recipeDetails, String username) {
        // Set creator
        recipeDetails.setCreatedBy(username);

        // Ensure category exists and get collection name
        String category = recipeDetails.getCategory();
        if (category == null || category.trim().isEmpty()) {
            category = "uncategorized";
            recipeDetails.setCategory(category);
        }

        categoryService.ensureCategoryExists(category);
        String collectionName = CategoryService.formatCollectionName(category);

        // Save to appropriate collection
        return mongoTemplate.insert(recipeDetails, collectionName);
    }

    @Override
    public List<Recipe> getAllRecipes() {
        List<Recipe> allRecipes = new ArrayList<>();

        // Get all categories and fetch recipes from each
        for (String category : categoryService.getAllCategories()) {
            String collectionName = CategoryService.formatCollectionName(category);
            List<Recipe> categoryRecipes = mongoTemplate.findAll(Recipe.class, collectionName);
            allRecipes.addAll(categoryRecipes);
        }

        return allRecipes;
    }

    @Override
    public Recipe getRecipeById(String category, String id) {
        String collectionName = CategoryService.formatCollectionName(category);
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, Recipe.class, collectionName);
    }

    @Override
    public Recipe getRecipeById(String id) {
        for (String category : categoryService.getAllCategories()) {
            Recipe recipe = getRecipeById(category, id);
            if (recipe != null) {
                return recipe;
            }
        }
        throw new RuntimeException("Recipe not found with id: " + id);
    }

    @Override
    public Recipe updateRecipe(String id, Recipe recipeDetails) {
        Recipe existingRecipe = getRecipeById(id);

        // If category has changed, move to new collection
        String oldCategory = existingRecipe.getCategory();
        String newCategory = recipeDetails.getCategory();

        if (!oldCategory.equals(newCategory)) {
            // Delete from old collection
            deleteRecipe(id);

            // Reset ID and save to new collection
            recipeDetails.setId(null);
            return createRecipe(recipeDetails, recipeDetails.getCreatedBy());
        }

        // Otherwise, update in place
        String collectionName = CategoryService.formatCollectionName(oldCategory);

        existingRecipe.setTitle(recipeDetails.getTitle());
        existingRecipe.setIngredients(recipeDetails.getIngredients());
        existingRecipe.setInstructions(recipeDetails.getInstructions());
        existingRecipe.setCookingTime(recipeDetails.getCookingTime());

        return mongoTemplate.save(existingRecipe, collectionName);
    }

    @Override
    public void deleteRecipe(String id) {
        Recipe recipe = getRecipeById(id);
        String collectionName = CategoryService.formatCollectionName(recipe.getCategory());

        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, Recipe.class, collectionName);
    }

    @Override
    public List<Recipe> getRecipesByUser(String username) {
        List<Recipe> userRecipes = new ArrayList<>();

        for (String category : categoryService.getAllCategories()) {
            String collectionName = CategoryService.formatCollectionName(category);
            Query query = new Query(Criteria.where("createdBy").is(username));
            List<Recipe> categoryUserRecipes = mongoTemplate.find(query, Recipe.class, collectionName);
            userRecipes.addAll(categoryUserRecipes);
        }

        return userRecipes;
    }

    @Override
    public List<Recipe> searchRecipesByTitle(String title) {
        List<Recipe> matchingRecipes = new ArrayList<>();

        for (String category : categoryService.getAllCategories()) {
            String collectionName = CategoryService.formatCollectionName(category);
            Query query = new Query(Criteria.where("title").regex(title, "i")); // Case-insensitive search
            List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
            matchingRecipes.addAll(categoryMatches);
        }

        return matchingRecipes;
    }

    @Override
    public List<Recipe> searchRecipesByCategory(String category) {
        String collectionName = CategoryService.formatCollectionName(category);
        if (mongoTemplate.collectionExists(collectionName)) {
            return mongoTemplate.findAll(Recipe.class, collectionName);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Recipe> searchRecipesByCookingTime(Integer cookingTime) {
        List<Recipe> matchingRecipes = new ArrayList<>();

        for (String category : categoryService.getAllCategories()) {
            String collectionName = CategoryService.formatCollectionName(category);
            Query query = new Query(Criteria.where("cookingTime").lte(cookingTime));
            List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
            matchingRecipes.addAll(categoryMatches);
        }

        return matchingRecipes;
    }

    @Override
    public List<Recipe> searchRecipesByIngredient(String ingredient) {
        List<Recipe> matchingRecipes = new ArrayList<>();

        for (String category : categoryService.getAllCategories()) {
            String collectionName = CategoryService.formatCollectionName(category);
            Query query = new Query(Criteria.where("ingredients").regex(ingredient, "i")); // Case-insensitive search
            List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
            matchingRecipes.addAll(categoryMatches);
        }

        return matchingRecipes;
    }

    @Override
    public List<Recipe> advancedSearch(String title, String category, Integer maxCookingTime, String ingredient) {
        List<Recipe> results;

        // If specific category is provided, search only in that collection
        if (category != null && !category.isEmpty()) {
            String collectionName = CategoryService.formatCollectionName(category);
            if (!mongoTemplate.collectionExists(collectionName)) {
                return new ArrayList<>();
            }

            Query query = new Query();
            if (title != null && !title.isEmpty()) {
                query.addCriteria(Criteria.where("title").regex(title, "i"));
            }
            if (maxCookingTime != null) {
                query.addCriteria(Criteria.where("cookingTime").lte(maxCookingTime));
            }
            if (ingredient != null && !ingredient.isEmpty()) {
                query.addCriteria(Criteria.where("ingredients").regex(ingredient, "i"));
            }

            results = mongoTemplate.find(query, Recipe.class, collectionName);
        } else {
            // Search across all categories
            results = new ArrayList<>();

            for (String cat : categoryService.getAllCategories()) {
                String collectionName = CategoryService.formatCollectionName(cat);

                Query query = new Query();
                if (title != null && !title.isEmpty()) {
                    query.addCriteria(Criteria.where("title").regex(title, "i"));
                }
                if (maxCookingTime != null) {
                    query.addCriteria(Criteria.where("cookingTime").lte(maxCookingTime));
                }
                if (ingredient != null && !ingredient.isEmpty()) {
                    query.addCriteria(Criteria.where("ingredients").regex(ingredient, "i"));
                }

                List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
                results.addAll(categoryMatches);
            }
        }

        return results;
    }
}