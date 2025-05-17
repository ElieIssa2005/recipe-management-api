package com.example.recipeoop_1.service;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.exception.RecipeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link RecipeService} interface.
 * <p>
 * This service class provides the concrete logic for managing recipes,
 * including CRUD operations and various search functionalities. It interacts
 * directly with {@link MongoTemplate} for database operations and uses
 * {@link CategoryService} to manage recipe categories, which are stored
 * as separate MongoDB collections (e.g., "recipe_desserts").
 * When a recipe is deleted, if its category collection becomes empty and is not
 * the default "uncategorized" collection, the category collection itself is deleted.
 * Similarly, when a recipe's category is updated, if the old category collection
 * becomes empty, it is also deleted (unless it's "uncategorized").
 * </p>
 *
 * @author Your Name/Team Name (Original authors: Elie Issa - Michel Ghazaly, as per project context)
 * @version 1.3
 * @since 2025-05-17
 * @see RecipeService
 * @see MongoTemplate
 * @see CategoryService
 * @see Recipe
 * @see RecipeNotFoundException
 */
@Service
public class RecipeServiceImpl implements RecipeService {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(RecipeServiceImpl.class);

    /**
     * MongoTemplate for database interactions.
     */
    private final MongoTemplate mongoTemplate;
    /**
     * CategoryService for managing categories.
     */
    private final CategoryService categoryService;

    /**
     * Name of the collection used for recipes that are not explicitly categorized
     * or whose category name resolves to empty. This collection will not be
     * automatically deleted even if it becomes empty.
     */
    private static final String UNCATEGORIZED_COLLECTION_NAME = "recipe_uncategorized";

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
        recipeDetails.setCreatedBy(username);

        String category = recipeDetails.getCategory();
        if (category == null || category.trim().isEmpty()) {
            category = "uncategorized"; // Default category
            recipeDetails.setCategory(category);
        }

        categoryService.ensureCategoryExists(category);
        String collectionName = CategoryService.formatCollectionName(category);

        log.info("Creating recipe '{}' in collection '{}' by user '{}'", recipeDetails.getTitle(), collectionName, username);
        return mongoTemplate.insert(recipeDetails, collectionName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all known category collections (obtained via
     * {@link CategoryService#getAllCategories()}), fetches all recipes from each,
     * and aggregates them into a single list. It defensively checks if a collection
     * exists before querying.
     * </p>
     */
    @Override
    public List<Recipe> getAllRecipes() {
        List<Recipe> allRecipes = new ArrayList<>();
        List<String> categories = categoryService.getAllCategories();
        log.debug("Fetching all recipes from categories: {}", categories);

        for (String categoryName : categories) {
            String collectionName = CategoryService.formatCollectionName(categoryName);
            if (mongoTemplate.collectionExists(collectionName)) {
                List<Recipe> categoryRecipes = mongoTemplate.findAll(Recipe.class, collectionName);
                allRecipes.addAll(categoryRecipes);
            } else {
                log.warn("Collection '{}' for category '{}' not found during getAllRecipes, though category was listed.", collectionName, categoryName);
            }
        }
        return allRecipes;
    }

    /**
     * {@inheritDoc}
     * This implementation retrieves a recipe from the MongoDB collection corresponding
     * to the given category name. The category name is first formatted using
     * {@link CategoryService#formatCollectionName(String)}.
     *
     * @return The found {@link Recipe}, or {@code null} if not found in the specified category.
     */
    @Override
    public Recipe getRecipeById(String category, String id) {
        String collectionName = CategoryService.formatCollectionName(category);
        Query query = new Query(Criteria.where("id").is(id));
        log.debug("Fetching recipe by ID '{}' from category '{}' (collection '{}')", id, category, collectionName);
        Recipe recipe = mongoTemplate.findOne(query, Recipe.class, collectionName);
        if (recipe == null) {
            log.warn("Recipe with ID '{}' not found in collection '{}'", id, collectionName);
        }
        return recipe;
    }

    /**
     * {@inheritDoc}
     * This implementation iterates through all known category collections to find the recipe.
     * For each category, it attempts to find the recipe using {@link #getRecipeById(String, String)}.
     * The first matching recipe found is returned.
     *
     * @param id The unique ID of the recipe to retrieve. Must not be {@code null} or empty.
     * @return The found {@link Recipe} object.
     * @throws RecipeNotFoundException if no recipe with the given ID exists in any category.
     */
    @Override
    public Recipe getRecipeById(String id) throws RecipeNotFoundException { // This method's @Override is likely line 188
        log.debug("Attempting to find recipe by ID '{}' across all categories", id);
        List<String> categories = categoryService.getAllCategories();
        for (String categoryName : categories) {
            Recipe recipe = getRecipeById(categoryName, id);
            if (recipe != null) {
                log.info("Found recipe ID '{}' in category '{}'", id, categoryName);
                return recipe;
            }
        }
        log.warn("Recipe with ID '{}' not found in any category.", id);
        throw new RecipeNotFoundException("Recipe not found with id: " + id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation first retrieves the existing recipe using {@link #getRecipeById(String)}.
     * If the category of the recipe has changed in {@code recipeDetails}:
     * <ol>
     * <li>The existing recipe is deleted from its old category collection.</li>
     * <li>If the old category collection becomes empty (and is not "uncategorized"), it is dropped.</li>
     * <li>The recipe (with its original ID and creator) is then re-created in the new category's collection.
     * The new category collection is ensured to exist.</li>
     * </ol>
     * If the category has not changed, the recipe is updated in place within its current collection.
     * </p>
     * @throws RecipeNotFoundException if the recipe to update is not found.
     */
    @Override
    public Recipe updateRecipe(String id, Recipe recipeDetails) throws RecipeNotFoundException {
        Recipe existingRecipe = getRecipeById(id);
        log.info("Updating recipe ID '{}', current title '{}'", id, existingRecipe.getTitle());

        String oldCategoryUserFriendly = existingRecipe.getCategory();
        String newCategoryUserFriendly = recipeDetails.getCategory();

        if (newCategoryUserFriendly == null || newCategoryUserFriendly.trim().isEmpty()) {
            newCategoryUserFriendly = "uncategorized";
            recipeDetails.setCategory(newCategoryUserFriendly);
        }

        String oldCollectionName = CategoryService.formatCollectionName(oldCategoryUserFriendly);
        String newCollectionName = CategoryService.formatCollectionName(newCategoryUserFriendly);

        if (!oldCollectionName.equalsIgnoreCase(newCollectionName)) {
            log.info("Category changed for recipe ID '{}'. Moving from collection '{}' to '{}'.", id, oldCollectionName, newCollectionName);
            Query deleteQuery = new Query(Criteria.where("id").is(id));
            mongoTemplate.remove(deleteQuery, Recipe.class, oldCollectionName);
            log.debug("Removed recipe ID '{}' from old collection '{}'", id, oldCollectionName);

            if (mongoTemplate.collectionExists(oldCollectionName) &&
                    !oldCollectionName.equalsIgnoreCase(UNCATEGORIZED_COLLECTION_NAME) &&
                    mongoTemplate.count(new Query(), oldCollectionName) == 0) {
                log.info("Old collection '{}' is now empty and not 'uncategorized'. Deleting collection.", oldCollectionName);
                mongoTemplate.dropCollection(oldCollectionName);
            }

            recipeDetails.setId(existingRecipe.getId());
            recipeDetails.setCreatedBy(existingRecipe.getCreatedBy());
            categoryService.ensureCategoryExists(newCategoryUserFriendly);
            log.debug("Inserting recipe ID '{}' into new collection '{}'", id, newCollectionName);
            return mongoTemplate.insert(recipeDetails, newCollectionName);
        } else {
            log.debug("Category for recipe ID '{}' remains collection '{}'. Updating in place.", id, newCollectionName);
            recipeDetails.setId(id);
            recipeDetails.setCreatedBy(existingRecipe.getCreatedBy());
            return mongoTemplate.save(recipeDetails, newCollectionName);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation first retrieves the recipe using {@link #getRecipeById(String)}
     * to determine its category. Then, it constructs a query to remove the recipe
     * from the appropriate category-specific collection. If, after deletion, the category
     * collection becomes empty and is not the "uncategorized" collection, the collection
     * itself is dropped from the database.
     * </p>
     * @throws RecipeNotFoundException if the recipe to delete is not found.
     */
    @Override
    public void deleteRecipe(String id) throws RecipeNotFoundException {
        Recipe recipe = getRecipeById(id);
        String collectionName = CategoryService.formatCollectionName(recipe.getCategory());
        log.info("Deleting recipe ID '{}' with title '{}' from collection '{}'", id, recipe.getTitle(), collectionName);

        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, Recipe.class, collectionName);
        log.debug("Recipe ID '{}' removed from collection '{}'", id, collectionName);

        if (mongoTemplate.collectionExists(collectionName) &&
                !collectionName.equalsIgnoreCase(UNCATEGORIZED_COLLECTION_NAME)) {
            long count = mongoTemplate.count(new Query(), collectionName);
            log.debug("Collection '{}' now has {} recipes.", collectionName, count);
            if (count == 0) {
                log.info("Collection '{}' is now empty and is not the default 'uncategorized' collection. Deleting collection.", collectionName);
                mongoTemplate.dropCollection(collectionName);
                log.info("Collection '{}' dropped.", collectionName);
            }
        } else if (collectionName.equalsIgnoreCase(UNCATEGORIZED_COLLECTION_NAME)) {
            log.debug("Collection '{}' is the default 'uncategorized' collection and will not be automatically deleted even if empty.", collectionName);
        } else {
            log.debug("Collection '{}' did not exist or was already dropped when checking if empty post-delete.", collectionName);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all known category collections, queries each
     * for recipes where the {@code createdBy} field matches the given username,
     * and aggregates the results. It defensively checks if a collection
     * exists before querying.
     * </p>
     */
    @Override
    public List<Recipe> getRecipesByUser(String username) {
        List<Recipe> userRecipes = new ArrayList<>();
        log.debug("Fetching recipes created by user '{}'", username);
        List<String> categories = categoryService.getAllCategories();

        for (String categoryName : categories) {
            String collectionName = CategoryService.formatCollectionName(categoryName);
            if(mongoTemplate.collectionExists(collectionName)) {
                Query query = new Query(Criteria.where("createdBy").is(username));
                List<Recipe> categoryUserRecipes = mongoTemplate.find(query, Recipe.class, collectionName);
                userRecipes.addAll(categoryUserRecipes);
            }
        }
        return userRecipes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all category collections, performing a case-insensitive
     * regular expression search on the {@code title} field within each collection.
     * All matching recipes are aggregated. It defensively checks if a collection
     * exists before querying.
     * </p>
     */
    @Override
    public List<Recipe> searchRecipesByTitle(String title) {
        List<Recipe> matchingRecipes = new ArrayList<>();
        String regexPattern = (title != null) ? title.trim() : "";
        log.debug("Searching for recipes with title containing '{}'", regexPattern);
        List<String> categories = categoryService.getAllCategories();

        for (String categoryName : categories) {
            String collectionName = CategoryService.formatCollectionName(categoryName);
            if(mongoTemplate.collectionExists(collectionName)) {
                Query query = new Query(Criteria.where("title").regex(regexPattern, "i"));
                List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
                matchingRecipes.addAll(categoryMatches);
            }
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
        log.debug("Searching for recipes in category '{}' (collection '{}')", category, collectionName);
        if (mongoTemplate.collectionExists(collectionName)) {
            return mongoTemplate.findAll(Recipe.class, collectionName);
        }
        log.warn("Category '{}' (collection '{}') not found for search.", category, collectionName);
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all category collections, querying each for recipes
     * where the {@code cookingTime} field is less than or equal to ({@code lte}) the specified time.
     * All matching recipes are aggregated. It defensively checks if a collection
     * exists before querying.
     * </p>
     */
    @Override
    public List<Recipe> searchRecipesByCookingTime(Integer cookingTime) {
        List<Recipe> matchingRecipes = new ArrayList<>();
        if (cookingTime == null || cookingTime < 0) {
            log.warn("Invalid cooking time for search: {}", cookingTime);
            return matchingRecipes;
        }
        log.debug("Searching for recipes with cooking time <= {} minutes", cookingTime);
        List<String> categories = categoryService.getAllCategories();

        for (String categoryName : categories) {
            String collectionName = CategoryService.formatCollectionName(categoryName);
            if(mongoTemplate.collectionExists(collectionName)) {
                Query query = new Query(Criteria.where("cookingTime").lte(cookingTime));
                List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
                matchingRecipes.addAll(categoryMatches);
            }
        }
        return matchingRecipes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates through all category collections, performing a case-insensitive
     * regular expression search on the {@code ingredients} array/list field within each collection.
     * All matching recipes are aggregated. It defensively checks if a collection
     * exists before querying.
     * </p>
     */
    @Override
    public List<Recipe> searchRecipesByIngredient(String ingredient) {
        List<Recipe> matchingRecipes = new ArrayList<>();
        String regexPattern = (ingredient != null) ? ingredient.trim() : "";
        log.debug("Searching for recipes containing ingredient '{}'", regexPattern);
        List<String> categories = categoryService.getAllCategories();

        for (String categoryName : categories) {
            String collectionName = CategoryService.formatCollectionName(categoryName);
            if(mongoTemplate.collectionExists(collectionName)) {
                Query query = new Query(Criteria.where("ingredients").regex(regexPattern, "i"));
                List<Recipe> categoryMatches = mongoTemplate.find(query, Recipe.class, collectionName);
                matchingRecipes.addAll(categoryMatches);
            }
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
     * It defensively checks if collections exist before querying.
     * </p>
     */
    @Override
    public List<Recipe> advancedSearch(String title, String category, Integer maxCookingTime, String ingredient) {
        List<Recipe> results;
        log.debug("Performing advanced search with title: '{}', category: '{}', maxCookingTime: {}, ingredient: '{}'",
                title, category, maxCookingTime, ingredient);

        String searchTitle = (title != null) ? title.trim() : null;
        String searchCategory = (category != null) ? category.trim() : null;
        String searchIngredient = (ingredient != null) ? ingredient.trim() : null;

        if (searchCategory != null && !searchCategory.isEmpty()) {
            String collectionName = CategoryService.formatCollectionName(searchCategory);
            if (!mongoTemplate.collectionExists(collectionName)) {
                log.warn("Advanced search: Category '{}' (collection '{}') does not exist.", searchCategory, collectionName);
                return new ArrayList<>();
            }

            Query query = new Query();
            if (searchTitle != null && !searchTitle.isEmpty()) {
                query.addCriteria(Criteria.where("title").regex(searchTitle, "i"));
            }
            if (maxCookingTime != null && maxCookingTime >= 0) {
                query.addCriteria(Criteria.where("cookingTime").lte(maxCookingTime));
            }
            if (searchIngredient != null && !searchIngredient.isEmpty()) {
                query.addCriteria(Criteria.where("ingredients").regex(searchIngredient, "i"));
            }
            results = mongoTemplate.find(query, Recipe.class, collectionName);
        } else {
            results = new ArrayList<>();
            List<String> allCategories = categoryService.getAllCategories();
            for (String catName : allCategories) {
                String collectionName = CategoryService.formatCollectionName(catName);
                if (!mongoTemplate.collectionExists(collectionName)) continue;

                Query query = new Query();

                if (searchTitle != null && !searchTitle.isEmpty()) {
                    query.addCriteria(Criteria.where("title").regex(searchTitle, "i"));
                }
                if (maxCookingTime != null && maxCookingTime >= 0) {
                    query.addCriteria(Criteria.where("cookingTime").lte(maxCookingTime));
                }
                if (searchIngredient != null && !searchIngredient.isEmpty()) {
                    query.addCriteria(Criteria.where("ingredients").regex(searchIngredient, "i"));
                }
                results.addAll(mongoTemplate.find(query, Recipe.class, collectionName));
            }
        }
        log.info("Advanced search found {} results.", results.size());
        return results;
    }
}