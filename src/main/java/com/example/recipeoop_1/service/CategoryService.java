package com.example.recipeoop_1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing recipe categories.
 * <p>
 * This service interacts with MongoDB to dynamically determine available categories
 * based on collection naming conventions (collections prefixed with "recipe_").
 * It also provides functionality to ensure that a collection exists for a given category
 * and a utility method to format category names into valid MongoDB collection names.
 * </p><p>
 * The strategy of using separate MongoDB collections for each category (e.g., "recipe_desserts",
 * "recipe_main_course") is managed here.
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.2
 * @since 2025-05-14
 * @see MongoTemplate
 * @see Service
 */
@Service
public class CategoryService {

    /**
     * The Spring Data {@link MongoTemplate} for interacting with MongoDB.
     * This is injected by Spring and used for all database operations within this service.
     */
    private final MongoTemplate mongoTemplate;

    /**
     * Constructs a {@code CategoryService} with the necessary {@link MongoTemplate}.
     *
     * @param mongoTemplate The Spring Data {@link MongoTemplate} for interacting with MongoDB.
     */
    @Autowired
    public CategoryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Retrieves a list of all available recipe category names.
     * <p>
     * Categories are derived by listing all collection names in the MongoDB database
     * that start with the prefix "recipe_". The "recipe_" prefix is then removed
     * to yield the user-friendly category name.
     * </p>
     * Example: If collections "recipe_desserts" and "recipe_main_course" exist,
     * this method will return a list containing "desserts" and "main_course".
     *
     * @return A {@link List} of strings, where each string is a discovered category name.
     * Returns an empty list if no collections matching the pattern are found.
     */
    public List<String> getAllCategories() {
        return mongoTemplate.getCollectionNames().stream()
                .filter(name -> name.startsWith("recipe_"))
                .map(name -> name.substring("recipe_".length()))
                .collect(Collectors.toList());
    }

    /**
     * Ensures that a MongoDB collection exists for the specified category.
     * <p>
     * If a collection corresponding to the formatted category name (e.g., "recipe_main_course"
     * for category "Main Course") does not already exist, it will be created.
     * This method is useful for dynamically managing collections as new categories are introduced.
     * </p>
     *
     * @param category The user-friendly category name (e.g., "Desserts", "Main Course").
     * This name will be formatted before checking/creating the collection.
     * @see #formatCollectionName(String)
     */
    public void ensureCategoryExists(String category) {
        String collectionName = formatCollectionName(category);
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }
    }

    /**
     * Formats a given category string into a MongoDB-safe collection name.
     * <p>
     * The formatting process involves:
     * </p>
     * <ol>
     * <li>Converting the category name to lowercase.</li>
     * <li>Trimming leading and trailing whitespace.</li>
     * <li>Replacing sequences of one or more whitespace characters with a single underscore ("_").</li>
     * <li>Prepending "recipe_" to the formatted string.</li>
     * </ol>
     * Example: " Main Course " would be formatted to "recipe_main_course".
     *
     * @param category The user-friendly category name to format.
     * @return A string representing the formatted, MongoDB-safe collection name.
     */
    public static String formatCollectionName(String category) {
        if (category == null || category.trim().isEmpty()) {
            category = (category == null) ? "uncategorized" : category.trim();
            if (category.isEmpty()) category = "uncategorized";
        }
        String formatted = category.toLowerCase().trim().replaceAll("\\s+", "_");
        return "recipe_" + formatted;
    }
}