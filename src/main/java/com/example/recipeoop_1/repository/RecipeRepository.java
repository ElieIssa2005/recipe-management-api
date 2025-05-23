package com.example.recipeoop_1.repository;

import com.example.recipeoop_1.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository interface for {@link Recipe} entities.
 * <p>
 * This interface extends {@link MongoRepository}, providing standard CRUD (Create, Read, Update, Delete)
 * operations for {@link Recipe} objects. It also defines custom query methods for finding recipes
 * based on various criteria such as creator, title, category, and cooking time.
 * </p>
 * <p>
 * Note: While this repository is defined for a default "recipes" collection (as per {@link Recipe#Recipe()}),
 * the actual data storage might be distributed across multiple category-specific collections
 * (e.g., "recipe_desserts", "recipe_maincourse") by the service layer (e.g., {@code RecipeServiceImpl}).
 * This repository's methods, especially the custom {@code @Query} ones, might be intended for use
 * with a {@link org.springframework.data.mongodb.core.MongoTemplate} against specific collection names
 * rather than direct invocation if recipes are indeed stored in separate collections per category.
 * If recipes are in a single collection, these methods would work as standard Spring Data derived queries or custom queries.
 * The Javadoc for each method assumes it operates on a collection containing {@link Recipe} documents
 * that match the query criteria.
 * </p>
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.0
 * @since 2025-05-14
 * @see MongoRepository
 * @see Recipe
 * @see Page
 * @see Pageable
 */
@Repository
public interface RecipeRepository extends MongoRepository<Recipe, String> {

    /**
     * Finds a paginated list of recipes created by a specific user.
     * <p>
     * This method derives its query from the method name and parameters.
     * It searches for {@link Recipe} documents where the {@code createdBy} field matches
     * the provided username.
     * </p>
     *
     * @param username The username of the creator whose recipes are to be retrieved.
     * @param pageable A {@link Pageable} object specifying pagination information (page number, size, sort order).
     * @return A {@link Page} of {@link Recipe} objects created by the specified user,
     * respecting the pagination parameters.
     */
    // Original comment: Find recipes by createdBy field with pagination
    Page<Recipe> findByCreatedBy(String username, Pageable pageable);

    /**
     * Finds recipes whose titles contain the given keyword, ignoring case.
     * <p>
     * This method uses a custom MongoDB query defined by the {@link Query} annotation.
     * The query performs a regular expression search (case-insensitive) on the {@code title} field.
     * </p>
     * The MongoDB query is: {@code {'title': {$regex: ?0, $options: 'i'}}}
     * where {@code ?0} is a placeholder for the first method parameter (the title).
     *
     * @param title The keyword to search for within recipe titles.
     * @return A list of {@link Recipe} objects whose titles contain the keyword.
     * Returns an empty list if no matches are found.
     */
    // Original comment: Find recipes by title (case-insensitive)
    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    /**
     * Finds recipes belonging to a category whose name contains the given keyword, ignoring case.
     * <p>
     * This method uses a custom MongoDB query defined by the {@link Query} annotation.
     * The query performs a regular expression search (case-insensitive) on the {@code category} field.
     * </p>
     * The MongoDB query is: {@code {'category': {$regex: ?0, $options: 'i'}}}
     * where {@code ?0} is a placeholder for the first method parameter (the category).
     *
     * @param category The keyword to search for within recipe category names.
     * @return A list of {@link Recipe} objects whose category names contain the keyword.
     * Returns an empty list if no matches are found.
     */
    // Original comment: Find recipes by category (case-insensitive)
    @Query("{'category': {$regex: ?0, $options: 'i'}}")
    List<Recipe> findByCategoryContainingIgnoreCase(String category);

    /**
     * Finds recipes whose cooking time is less than or equal to the specified number of minutes.
     * <p>
     * This method derives its query from the method name. It searches for {@link Recipe}
     * documents where the {@code cookingTime} field is less than or equal to the provided value.
     * </p>
     *
     * @param cookingTime The maximum cooking time in minutes.
     * @return A list of {@link Recipe} objects that meet the cooking time criteria.
     * Returns an empty list if no such recipes are found.
     */
    // Original comment: Find recipes by cooking time less than or equal to specified minutes
    List<Recipe> findByCookingTimeLessThanEqual(Integer cookingTime);
}