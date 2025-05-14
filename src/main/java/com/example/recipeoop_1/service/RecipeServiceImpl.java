package com.example.recipeoop_1.service;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.exception.RecipeNotFoundException; // Import custom exception
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
// import java.util.Optional; // Not used
// import java.util.stream.Collectors; // Not used directly in this snippet

/**
 * Implementation of the {@link RecipeService} interface.
 * <p>
 * This service class provides the concrete logic for managing recipes,
 * including CRUD operations and various search functionalities. It interacts
 * directly with {@link MongoTemplate} for database operations and uses
 * {@link CategoryService} to manage recipe categories, which are stored
 * as separate MongoDB collections (e.g., "recipe_desserts").
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.0
 * @since 2025-05-14
 * @see RecipeService
 * @see MongoTemplate
 * @see CategoryService
 * @see Recipe
 */
@Service
public class RecipeServiceImpl implements RecipeService {

    private final MongoTemplate mongoTemplate;
    private final CategoryService categoryService;

    /**
     * Constructs a {@code RecipeServiceImpl} with the necessary dependencies.
     *
     * @param mongoTemplate The Spring Data {@link MongoTemplate} for direct MongoDB interaction.
     * @param categoryService The {@link CategoryService} for managing category-specific logic,
     * such as ensuring category collections exist and formatting names.
     */
    @Autowired
    public RecipeServiceImpl(MongoTemplate mongoTemplate, CategoryService categoryService) {
        this.mongoTemplate = mongoTemplate;
        this.categoryService = categoryService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the creator's username on the recipe. It ensures that
     * a MongoDB collection exists for the recipe's category (creating it if necessary
     * via {@link CategoryService#ensureCategoryExists(String)}). If the category is null or empty,
     * it defaults to "uncategorized". The recipe is then inserted into the
     * appropriately named collection (e.g., "recipe_main_course").
     * </p>
     */
    @Override
    public Recipe createRecipe(Recipe recipeDetails, String username) {
        // Set creator
        recipeDetails.setCreatedBy(username);

        // Ensure category exists and get collection name
        String category = recipeDetails.getCategory();
        if (category == null || category.trim().isEmpty()) {
            category = "uncategorized"; // Default category
            recipeDetails.setCategory(category);
        }

        categoryService.ensureCategoryExists(category);
        String collectionName = CategoryService.formatCollectionName(category);

        // Save to appropriate collection
        return mongoTemplate.insert(recipeDetails, collectionName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all known category collections (obtained via
     * {@link CategoryService#getAllCategories()}), fetches all recipes from each,
     * and aggregates them into a single list.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieves a recipe from the MongoDB collection corresponding
     * to the given category name. The category name is first formatted using
     * {@link CategoryService#formatCollectionName(String)}.
     * </p>
     * @return The found {@link Recipe}, or {@code null} if not found in the specified category.
     */
    @Override
    public Recipe getRecipeById(String category, String id) {
        String collectionName = CategoryService.formatCollectionName(category);
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, Recipe.class, collectionName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all known category collections. For each category,
     * it attempts to find the recipe using {@link #getRecipeById(String, String)}.
     * The first matching recipe found is returned.
     * </p>
     * @throws RecipeNotFoundException if the recipe with the given ID is not found in any category.
     */
    @Override
    public Recipe getRecipeById(String id) throws RecipeNotFoundException {
        for (String category : categoryService.getAllCategories()) {
            Recipe recipe = getRecipeById(category, id); // Uses the overloaded method
            if (recipe != null) {
                return recipe;
            }
        }
        throw new RecipeNotFoundException("Recipe not found with id: " + id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation first retrieves the existing recipe using {@link #getRecipeById(String)}.
     * If the category of the recipe has changed in {@code recipeDetails}, the existing recipe
     * is deleted from its old category collection, its ID is cleared (to allow MongoDB to generate a new one
     * if necessary, though typically for updates the ID should be preserved if possible, this logic might need review
     * if ID preservation across category moves is critical), and then it's re-created in the new category's collection
     * using {@link #createRecipe(Recipe, String)}.
     * If the category has not changed, the recipe is updated in place within its current collection
     * by setting its properties and calling {@link MongoTemplate#save(Object, String)}.
     * </p>
     * @throws RecipeNotFoundException if the recipe to update is not found.
     */
    @Override
    public Recipe updateRecipe(String id, Recipe recipeDetails) throws RecipeNotFoundException {
        Recipe existingRecipe = getRecipeById(id); // This will throw RecipeNotFoundException if not found

        String oldCategory = existingRecipe.getCategory();
        String newCategory = recipeDetails.getCategory();
        // Ensure newCategory is not null and defaults if empty, similar to createRecipe
        if (newCategory == null || newCategory.trim().isEmpty()) {
            newCategory = "uncategorized";
            recipeDetails.setCategory(newCategory);
        }


        if (!oldCategory.equalsIgnoreCase(newCategory)) { // Case-insensitive category comparison
            // Category has changed, effectively moving the recipe.
            // Delete from old collection.
            // The deleteRecipe method itself calls getRecipeById, which might be redundant here
            // but ensures consistency.
            deleteRecipe(id); // This uses the existingRecipe's ID and original category implicitly

            // Prepare for re-creation in new category.
            // Preserve the original ID and creator for the "moved" recipe.
            recipeDetails.setId(existingRecipe.getId()); // Preserve original ID
            recipeDetails.setCreatedBy(existingRecipe.getCreatedBy()); // Preserve original creator

            return createRecipe(recipeDetails, existingRecipe.getCreatedBy()); // Re-create in new category
        } else {
            // Category is the same, update in place.
            String collectionName = CategoryService.formatCollectionName(oldCategory);

            // Update fields of the existing recipe object before saving.
            // It's crucial to update the 'existingRecipe' instance fetched from DB,
            // then save that instance to ensure MongoDB's optimistic locking or versioning (if used) works.
            // However, the current code uses mongoTemplate.save which can perform an upsert if ID is present.
            // For clarity, explicitly setting fields on 'existingRecipe' is better if it's a true update.
            // The current approach of saving 'recipeDetails' with existing 'id' might be okay
            // if 'recipeDetails' is guaranteed to have the ID set by the controller/caller.
            // Let's assume 'recipeDetails' should have the 'id' from the path variable.
            recipeDetails.setId(id); // Ensure the ID from path is used for the save operation
            recipeDetails.setCreatedBy(existingRecipe.getCreatedBy()); // Preserve original creator

            // Update properties of existingRecipe with values from recipeDetails
            existingRecipe.setTitle(recipeDetails.getTitle());
            existingRecipe.setIngredients(recipeDetails.getIngredients());
            existingRecipe.setInstructions(recipeDetails.getInstructions());
            existingRecipe.setCookingTime(recipeDetails.getCookingTime());
            // Category is already ensured to be the same or updated on recipeDetails
            existingRecipe.setCategory(recipeDetails.getCategory());


            return mongoTemplate.save(existingRecipe, collectionName);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation first retrieves the recipe using {@link #getRecipeById(String)}
     * to determine its category. Then, it constructs a query to remove the recipe
     * from the appropriate category-specific collection using {@link MongoTemplate#remove(Query, Class, String)}.
     * </p>
     * @throws RecipeNotFoundException if the recipe to delete is not found.
     */
    @Override
    public void deleteRecipe(String id) throws RecipeNotFoundException {
        Recipe recipe = getRecipeById(id); // Throws RecipeNotFoundException if not found
        String collectionName = CategoryService.formatCollectionName(recipe.getCategory());

        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, Recipe.class, collectionName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all known category collections, queries each
     * for recipes where the {@code createdBy} field matches the given username,
     * and aggregates the results.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all category collections, performing a case-insensitive
     * regular expression search on the {@code title} field within each collection.
     * All matching recipes are aggregated.
     * </p>
     */
    @Override
    public List<Recipe> searchRecipesByTitle(String title) {
        List<Recipe> matchingRecipes = new ArrayList<>();
        String regexPattern = (title != null) ? title : ""; // Avoid NPE if title is null

        for (String category : categoryService.getAllCategories()) {
            String collectionName = CategoryService.formatCollectionName(category);
            Query query = new Query(Criteria.where("title").regex(regexPattern, "i")); // Case-insensitive search
            List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
            matchingRecipes.addAll(categoryMatches);
        }
        return matchingRecipes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation formats the given category name to a collection name using
     * {@link CategoryService#formatCollectionName(String)}. If the collection exists,
     * it retrieves all recipes from that collection. Otherwise, it returns an empty list.
     * </p>
     */
    @Override
    public List<Recipe> searchRecipesByCategory(String category) {
        String collectionName = CategoryService.formatCollectionName(category);
        if (mongoTemplate.collectionExists(collectionName)) {
            return mongoTemplate.findAll(Recipe.class, collectionName);
        }
        return new ArrayList<>(); // Return empty list if category (collection) doesn't exist
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all category collections, querying each for recipes
     * where the {@code cookingTime} field is less than or equal to ({@code lte}) the specified time.
     * All matching recipes are aggregated.
     * </p>
     */
    @Override
    public List<Recipe> searchRecipesByCookingTime(Integer cookingTime) {
        List<Recipe> matchingRecipes = new ArrayList<>();
        if (cookingTime == null || cookingTime < 0) { // Basic validation
            return matchingRecipes; // Or throw IllegalArgumentException
        }

        for (String category : categoryService.getAllCategories()) {
            String collectionName = CategoryService.formatCollectionName(category);
            Query query = new Query(Criteria.where("cookingTime").lte(cookingTime));
            List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
            matchingRecipes.addAll(categoryMatches);
        }
        return matchingRecipes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all category collections, performing a case-insensitive
     * regular expression search on the {@code ingredients} array/list field within each collection.
     * All matching recipes are aggregated.
     * </p>
     */
    @Override
    public List<Recipe> searchRecipesByIngredient(String ingredient) {
        List<Recipe> matchingRecipes = new ArrayList<>();
        String regexPattern = (ingredient != null) ? ingredient : ""; // Avoid NPE

        for (String category : categoryService.getAllCategories()) {
            String collectionName = CategoryService.formatCollectionName(category);
            // MongoDB regex search on array elements typically matches if any element in the array matches.
            Query query = new Query(Criteria.where("ingredients").regex(regexPattern, "i")); // Case-insensitive search
            List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
            matchingRecipes.addAll(categoryMatches);
        }
        return matchingRecipes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation builds a dynamic MongoDB {@link Query}.
     * If a specific {@code category} is provided and not empty, the search is confined to that
     * category's collection. Otherwise, it iterates through all category collections, applying
     * the filter criteria (title, maxCookingTime, ingredient) to each.
     * All criteria are optional and are added to the query only if provided.
     * Searches involving text (title, ingredient) are case-insensitive.
     * </p>
     */
    @Override
    public List<Recipe> advancedSearch(String title, String category, Integer maxCookingTime, String ingredient) {
        List<Recipe> results;

        // If specific category is provided, search only in that collection
        if (category != null && !category.trim().isEmpty()) {
            String collectionName = CategoryService.formatCollectionName(category);
            if (!mongoTemplate.collectionExists(collectionName)) {
                return new ArrayList<>(); // Category does not exist
            }

            Query query = new Query();
            if (title != null && !title.trim().isEmpty()) {
                query.addCriteria(Criteria.where("title").regex(title, "i"));
            }
            if (maxCookingTime != null && maxCookingTime >= 0) { // Ensure non-negative
                query.addCriteria(Criteria.where("cookingTime").lte(maxCookingTime));
            }
            if (ingredient != null && !ingredient.trim().isEmpty()) {
                query.addCriteria(Criteria.where("ingredients").regex(ingredient, "i"));
            }

            results = mongoTemplate.find(query, Recipe.class, collectionName);
        } else {
            // Search across all categories if no specific category is given
            results = new ArrayList<>();
            for (String cat : categoryService.getAllCategories()) {
                String collectionName = CategoryService.formatCollectionName(cat);
                Query query = new Query(); // New query for each category collection

                if (title != null && !title.trim().isEmpty()) {
                    query.addCriteria(Criteria.where("title").regex(title, "i"));
                }
                if (maxCookingTime != null && maxCookingTime >= 0) {
                    query.addCriteria(Criteria.where("cookingTime").lte(maxCookingTime));
                }
                if (ingredient != null && !ingredient.trim().isEmpty()) {
                    query.addCriteria(Criteria.where("ingredients").regex(ingredient, "i"));
                }

                // Only execute find if there are some criteria or if we want all from category
                // This condition avoids finding all recipes if all search terms are null/empty
                // and no category was specified (which is this 'else' block).
                // However, if the intent is to list all if no criteria, this check might be removed.
                if (!query.getQueryObject().isEmpty() || query.getSortObject().isEmpty()) {
                    List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
                    results.addAll(categoryMatches);
                } else if (query.getQueryObject().isEmpty() && title == null && maxCookingTime == null && ingredient == null) {
                    // If no criteria were provided at all (and no specific category),
                    // this would effectively be like getAllRecipes().
                    // This part of the logic might need clarification based on desired behavior
                    // when advancedSearch is called with all null/empty parameters.
                    // For now, it will add all recipes from the category if no criteria are set.
                    List<Recipe> categoryMatches = mongoTemplate.findAll(Recipe.class, collectionName);
                    results.addAll(categoryMatches);
                }
            }
        }
        return results;
    }
}